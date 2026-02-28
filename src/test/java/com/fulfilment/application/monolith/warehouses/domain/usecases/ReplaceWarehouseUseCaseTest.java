package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Replace Warehouse use case.
 *
 * Covers basic replace operations, validation rules, and concurrent modification scenarios.
 */
@QuarkusTest
class ReplaceWarehouseUseCaseTest {

  @Inject
  WarehouseRepository warehouseRepository;

  @Inject
  LocationGateway locationResolver;

  @Inject
  EntityManager em;

  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean slate
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();

    // Initialize use case
    replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseRepository, locationResolver);
  }

  /**
   * Basic replace functionality
   */
  @Test
  @Transactional
  void testReplaceWarehouseSuccessfully() {
    // Create a warehouse
    createWarehouse("REPLACE-TEST-001", "AMSTERDAM-001", 80, 40);

    // Replace it with new values
    Warehouse replacement = new Warehouse();
    replacement.setBusinessUnitCode("REPLACE-TEST-001");
    replacement.setLocation("ZWOLLE-001");
    replacement.setCapacity(30);
    replacement.setStock(15);

    replaceWarehouseUseCase.replace(replacement);

    // Verify it was replaced
    Warehouse updated = warehouseRepository.findByBusinessUnitCode("REPLACE-TEST-001");
    assertNotNull(updated);
    assertEquals("ZWOLLE-001", updated.getLocation());
    assertEquals(30, updated.getCapacity());
    assertEquals(15, updated.getStock());
  }

  /**
   * Cannot replace non-existent warehouse
   */
  @Test
  @Transactional
  void testCannotReplaceNonExistentWarehouse() {
    Warehouse replacement = new Warehouse();
    replacement.setBusinessUnitCode("NON-EXISTENT");
    replacement.setLocation("AMSTERDAM-001");
    replacement.setCapacity(50);
    replacement.setStock(25);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(replacement));

    assertTrue(exception.getMessage().contains("does not exist"));
  }

  /**
   * Cannot replace archived warehouse
   */
  @Test
  @Transactional
  void testCannotReplaceArchivedWarehouse() {
    // Create and archive a warehouse
    Warehouse warehouse = createWarehouse("REPLACE-TEST-002", "AMSTERDAM-001", 80, 40);
    warehouse.setArchivedAt(LocalDateTime.now());
    warehouseRepository.update(warehouse);

    // Try to replace it
    Warehouse replacement = new Warehouse();
    replacement.setBusinessUnitCode("REPLACE-TEST-002");
    replacement.setLocation("ZWOLLE-001");
    replacement.setCapacity(30);
    replacement.setStock(15);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(replacement));

    assertTrue(exception.getMessage().contains("archived"));
  }

  /**
   * Capacity and stock validations (parameterized)
   */
  @ParameterizedTest
  @MethodSource("provideInvalidReplaceScenarios")
  @Transactional
  void testCapacityAndStockValidations(InvalidReplaceScenario scenario) {
    // Create a baseline warehouse
    createWarehouse("REPLACE-VALIDATION", "AMSTERDAM-001", 80, 40);

    // Try to replace with invalid values
    Warehouse replacement = new Warehouse();
    replacement.setBusinessUnitCode("REPLACE-VALIDATION");
    replacement.setLocation(scenario.location);
    replacement.setCapacity(scenario.capacity);
    replacement.setStock(scenario.stock);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(replacement));

    assertTrue(exception.getMessage().contains(scenario.expectedMessageFragment),
        "Expected message to contain '" + scenario.expectedMessageFragment +
        "' but got: " + exception.getMessage());
  }

  /**
   * Concurrent replace scenario.
   *
   * Scenario:
   * - Thread 1: Replace warehouse with capacity=50
   * - Thread 2: Replace same warehouse with capacity=60 concurrently
   * - Expected: Data integrity is preserved — either the conflict is detected
   *             and an exception is thrown, or only one update is applied.
   */
  @Test
  void testConcurrentReplaceCausesLostUpdates() throws InterruptedException {
    // Setup: Create a warehouse
    String businessUnitCode = createWarehouseInNewTransaction("CONCURRENT-REPLACE-001", "AMSTERDAM-001", 100, 50);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(2);

    AtomicBoolean thread1Success = new AtomicBoolean(false);
    AtomicBoolean thread2Success = new AtomicBoolean(false);
    AtomicBoolean exceptionCaught = new AtomicBoolean(false);
    AtomicReference<Exception> caughtException = new AtomicReference<>();

    // Thread 1: Replace warehouse with capacity=50
    executor.submit(() -> {
      try {
        startLatch.await(); // Synchronize start
        replaceWarehouseInNewTransaction(businessUnitCode, "ZWOLLE-001", 50, 25);
        thread1Success.set(true);
      } catch (Exception e) {
        exceptionCaught.set(true);
        caughtException.set(e);
      } finally {
        finishLatch.countDown();
      }
    });

    // Thread 2: Replace same warehouse with capacity=60 (concurrently)
    executor.submit(() -> {
      try {
        startLatch.await(); // Synchronize start
        replaceWarehouseInNewTransaction(businessUnitCode, "TILBURG-001", 60, 30);
        thread2Success.set(true);
      } catch (Exception e) {
        exceptionCaught.set(true);
        caughtException.set(e);
      } finally {
        finishLatch.countDown();
      }
    });

    startLatch.countDown(); // Start both threads
    finishLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    // Verification: Check the final state
    Warehouse finalWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);

    boolean onlyOneThreadSucceeded = (thread1Success.get() && !thread2Success.get()) ||
                                     (!thread1Success.get() && thread2Success.get());

    assertTrue(onlyOneThreadSucceeded || exceptionCaught.get(),
        "Expected only one thread to succeed OR an OptimisticLockException. " +
        "Instead, both succeeded causing lost update: location=" + finalWarehouse.getLocation() +
        ", capacity=" + finalWarehouse.getCapacity() + ", stock=" + finalWarehouse.getStock());

    // If no exception was caught, verify that the final state matches one thread completely
    if (!exceptionCaught.get()) {
      boolean matchesThread1 = "ZWOLLE-001".equals(finalWarehouse.getLocation()) &&
                               finalWarehouse.getCapacity() == 50 &&
                               finalWarehouse.getStock() == 25;

      boolean matchesThread2 = "TILBURG-001".equals(finalWarehouse.getLocation()) &&
                               finalWarehouse.getCapacity() == 60 &&
                               finalWarehouse.getStock() == 30;

      assertTrue(matchesThread1 || matchesThread2,
          "Final state should match exactly one thread's update, not a mix of both");
    }
  }

  // Helper methods

  @Transactional(TxType.REQUIRES_NEW)
  Warehouse createWarehouse(String businessUnitCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode(businessUnitCode);
    warehouse.setLocation(location);
    warehouse.setCapacity(capacity);
    warehouse.setStock(stock);
    warehouse.setCreatedAt(LocalDateTime.now());

    warehouseRepository.create(warehouse);
    return warehouse;
  }

  @Transactional(TxType.REQUIRES_NEW)
  String createWarehouseInNewTransaction(String businessUnitCode, String location, int capacity, int stock) {
    createWarehouse(businessUnitCode, location, capacity, stock);
    return businessUnitCode;
  }

  @Transactional(TxType.REQUIRES_NEW)
  void replaceWarehouseInNewTransaction(String businessUnitCode, String newLocation, int newCapacity, int newStock) {
    Warehouse replacement = new Warehouse();
    replacement.setBusinessUnitCode(businessUnitCode);
    replacement.setLocation(newLocation);
    replacement.setCapacity(newCapacity);
    replacement.setStock(newStock);

    replaceWarehouseUseCase.replace(replacement);
  }

  // Parameterized test data

  static Stream<InvalidReplaceScenario> provideInvalidReplaceScenarios() {
    return Stream.of(
        // Invalid location
        new InvalidReplaceScenario("INVALID-LOCATION", 50, 25, "not valid"),

        // Capacity exceeds location max (AMSTERDAM-001 max capacity is 100)
        new InvalidReplaceScenario("AMSTERDAM-001", 150, 50, "exceeds location max capacity"),

        // Stock exceeds capacity
        new InvalidReplaceScenario("ZWOLLE-001", 30, 40, "exceeds warehouse capacity")
    );
  }

  static class InvalidReplaceScenario {
    String location;
    int capacity;
    int stock;
    String expectedMessageFragment;

    InvalidReplaceScenario(String location, int capacity, int stock, String expectedMessageFragment) {
      this.location = location;
      this.capacity = capacity;
      this.stock = stock;
      this.expectedMessageFragment = expectedMessageFragment;
    }

    @Override
    public String toString() {
      return "InvalidReplaceScenario{location='" + location + "', capacity=" + capacity +
             ", stock=" + stock + ", expected='" + expectedMessageFragment + "'}";
    }
  }
}
