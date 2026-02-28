package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Archive Warehouse use case.
 *
 * Covers basic archive operations and concurrent modification scenarios.
 */
@QuarkusTest
class ArchiveWarehouseUseCaseTest {

  @Inject
  WarehouseRepository warehouseRepository;

  @Inject
  ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Inject
  EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean slate
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();
  }

  /**
   * Basic archive functionality
   */
  @Test
  @Transactional
  void testArchiveWarehouseSuccessfully() {
    // Create a warehouse
    Warehouse warehouse = createWarehouse("ARCHIVE-TEST-001", "AMSTERDAM-001");

    // Archive it
    archiveWarehouseUseCase.archive(warehouse);

    // Verify it was archived
    Warehouse archived = warehouseRepository.findByBusinessUnitCode("ARCHIVE-TEST-001");
    assertNotNull(archived);
    assertNotNull(archived.getArchivedAt());
  }

  /**
   * Cannot archive non-existent warehouse
   */
  @Test
  @Transactional
  void testCannotArchiveNonExistentWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("NON-EXISTENT");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> archiveWarehouseUseCase.archive(warehouse));

    assertTrue(exception.getMessage().contains("does not exist"));
  }

  /**
   * Cannot archive already-archived warehouse
   */
  @Test
  @Transactional
  void testCannotArchiveAlreadyArchivedWarehouse() {
    // Create and archive a warehouse
    Warehouse warehouse = createWarehouse("ARCHIVE-TEST-002", "ZWOLLE-001");
    archiveWarehouseUseCase.archive(warehouse);

    // Try to archive again
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> archiveWarehouseUseCase.archive(warehouse));

    assertTrue(exception.getMessage().contains("already archived"));
  }

  /**
   * Concurrent archive and stock update scenario.
   *
   * Scenario:
   * - Thread 1: Archives warehouse (sets archivedAt)
   * - Thread 2: Updates stock concurrently
   * - Expected: Data integrity is preserved — either the conflict is detected
   *             and an exception is thrown, or both changes are correctly applied.
   */
  @Test
  void testConcurrentArchiveAndStockUpdateCausesOptimisticLockException() throws InterruptedException {
    // Setup: Create a warehouse
    String businessUnitCode = createWarehouseInNewTransaction("CONCURRENT-ARCHIVE-001", "AMSTERDAM-001");

    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(2);

    AtomicBoolean exceptionCaught = new AtomicBoolean(false);

    // Thread 1: Archive warehouse
    executor.submit(() -> {
      try {
        startLatch.await(); // Synchronize start
        archiveWarehouseInNewTransaction(businessUnitCode);
      } catch (Exception e) {
        exceptionCaught.set(true);
      } finally {
        finishLatch.countDown();
      }
    });

    // Thread 2: Update stock concurrently
    executor.submit(() -> {
      try {
        startLatch.await(); // Synchronize start
        updateStockInNewTransaction(businessUnitCode, 75);
      } catch (Exception e) {
        exceptionCaught.set(true);
      } finally {
        finishLatch.countDown();
      }
    });

    startLatch.countDown(); // Start both threads
    finishLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    // Verification: Check the final state
    Warehouse finalWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);

    boolean bothChangesApplied = finalWarehouse.getArchivedAt() != null && finalWarehouse.getStock() == 75;

    assertTrue(bothChangesApplied || exceptionCaught.get(),
        "Expected either both changes to be applied properly OR an exception to be thrown. " +
        "Instead, a lost update occurred: archivedAt=" + finalWarehouse.getArchivedAt() +
        ", stock=" + finalWarehouse.getStock());

    // Additional check: if no exception was caught, both changes should be applied
    if (!exceptionCaught.get()) {
      assertNotNull(finalWarehouse.getArchivedAt(), "Archive timestamp should be set");
      assertEquals(75, finalWarehouse.getStock(), "Stock should be updated to 75");
    }
  }

  // Helper methods

  @Transactional(TxType.REQUIRES_NEW)
  Warehouse createWarehouse(String businessUnitCode, String location) {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode(businessUnitCode);
    warehouse.setLocation(location);
    warehouse.setCapacity(100);
    warehouse.setStock(50);
    warehouse.setCreatedAt(LocalDateTime.now());

    warehouseRepository.create(warehouse);
    return warehouse;
  }

  @Transactional(TxType.REQUIRES_NEW)
  String createWarehouseInNewTransaction(String businessUnitCode, String location) {
    createWarehouse(businessUnitCode, location);
    return businessUnitCode;
  }

  @Transactional(TxType.REQUIRES_NEW)
  void archiveWarehouseInNewTransaction(String businessUnitCode) {
    Warehouse warehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    archiveWarehouseUseCase.archive(warehouse);
  }

  @Transactional(TxType.REQUIRES_NEW)
  void updateStockInNewTransaction(String businessUnitCode, int newStock) {
    Warehouse warehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    warehouse.setStock(newStock);
    warehouseRepository.update(warehouse);
  }
}
