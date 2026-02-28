package com.fulfilment.application.monolith.warehouses.adapters;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sophisticated Test: Testcontainers Integration Test
 * 
 * Uses real PostgreSQL database via Testcontainers instead of mocks.
 * Tests complex database scenarios, constraints, and queries against
 * actual database behavior.
 * 
 * Quarkus provides built-in support for spinning up test databases.
 */
@QuarkusTest
class WarehouseTestcontainersIT {

  @Inject
  WarehouseRepository warehouseRepository;

  @Inject
  LocationGateway locationResolver;

  @Inject
  EntityManager em;

  private CreateWarehouseUseCase createWarehouseUseCase;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean database
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();
    
    createWarehouseUseCase = new CreateWarehouseUseCase(warehouseRepository, locationResolver);
  }

  /**
   * Test database unique constraint on business unit code.
   */
  @Test
  @Transactional
  void testDatabaseUniqueConstraintOnBusinessUnitCode() {
    // Create first warehouse
    Warehouse warehouse1 = new Warehouse();
    warehouse1.setBusinessUnitCode("DB-UNIQUE-001");
    warehouse1.setLocation("AMSTERDAM-001");
    warehouse1.setCapacity(50);
    warehouse1.setStock(10);
    warehouse1.setCreatedAt(java.time.LocalDateTime.now());

    createWarehouseUseCase.create(warehouse1);
    
    // Try to create second with same code directly via DB
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.setBusinessUnitCode("DB-UNIQUE-001");  // Duplicate!
    dbWarehouse.setLocation("ZWOLLE-001");
    dbWarehouse.setCapacity(30);
    dbWarehouse.setStock(5);
    dbWarehouse.setCreatedAt(java.time.LocalDateTime.now());

    // Database should reject this
    assertThrows(Exception.class, () -> {
      em.persist(dbWarehouse);
      em.flush();
    });
  }

  /**
   * Test query performance and correctness with multiple warehouses.
   */
  @Test
  @Transactional
  void testQueryingMultipleWarehousesAtSameLocation() {
    // Create multiple warehouses at same location
    for (int i = 0; i < 5; i++) {
      Warehouse warehouse = new Warehouse();
      warehouse.setBusinessUnitCode("QUERY-TEST-" + i);
      warehouse.setLocation("AMSTERDAM-001");
      warehouse.setCapacity(20 + (i * 10));
      warehouse.setStock(5 + i);

      createWarehouseUseCase.create(warehouse);
    }
    
    // Query all warehouses
    List<Warehouse> all = warehouseRepository.getAll();
    
    // Should have at least 5
    assertTrue(all.size() >= 5);
    
    // Verify they're from Amsterdam
    long amsterdamCount = all.stream()
        .filter(w -> "AMSTERDAM-001".equals(w.getLocation()))
        .count();
    
    assertEquals(5, amsterdamCount);
  }

  /**
   * Test database handles NULL values correctly.
   */
  @Test
  @Transactional
  void testNullFieldsHandling() {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.setBusinessUnitCode("NULL-TEST-001");
    dbWarehouse.setLocation("ZWOLLE-001");
    dbWarehouse.setCapacity(50);
    dbWarehouse.setStock(10);
    dbWarehouse.setCreatedAt(java.time.LocalDateTime.now());
    dbWarehouse.setArchivedAt(null);  // NULL archived date

    em.persist(dbWarehouse);
    em.flush();
    
    // Retrieve and verify
    DbWarehouse found = em.find(DbWarehouse.class, dbWarehouse.getId());
    assertNotNull(found);
    assertNull(found.getArchivedAt());
  }

  /**
   * Test database transaction rollback behavior.
   */
  @Test
  void testTransactionRollbackDoesNotPersist() {
    try {
      performFailingTransaction();
    } catch (Exception e) {
      // Expected
    }
    
    // Verify nothing was persisted
    Warehouse found = warehouseRepository.findByBusinessUnitCode("ROLLBACK-TEST-001");
    assertNull(found, "Rolled back warehouse should not exist in database");
  }

  @Transactional
  void performFailingTransaction() {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode("ROLLBACK-TEST-001");
    warehouse.setLocation("TILBURG-001");
    warehouse.setCapacity(30);
    warehouse.setStock(10);

    createWarehouseUseCase.create(warehouse);
    
    // Force rollback
    throw new RuntimeException("Simulated failure");
  }

  /**
   * Test complex query: find warehouses by location and capacity range.
   */
  @Test
  @Transactional
  void testComplexQueryByLocationAndCapacity() {
    // Create warehouses with different capacities
    createWarehouse("COMPLEX-1", "AMSTERDAM-001", 30);
    createWarehouse("COMPLEX-2", "AMSTERDAM-001", 50);
    createWarehouse("COMPLEX-3", "AMSTERDAM-001", 70);
    createWarehouse("COMPLEX-4", "ZWOLLE-001", 40);
    
    // Query using JPQL
    List<DbWarehouse> results = em.createQuery(
        "SELECT w FROM DbWarehouse w WHERE w.location = :location AND w.capacity BETWEEN :min AND :max",
        DbWarehouse.class)
        .setParameter("location", "AMSTERDAM-001")
        .setParameter("min", 40)
        .setParameter("max", 70)
        .getResultList();
    
    // Should find COMPLEX-2 and COMPLEX-3
    assertEquals(2, results.size());
  }

  private void createWarehouse(String code, String location, int capacity) {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode(code);
    warehouse.setLocation(location);
    warehouse.setCapacity(capacity);
    warehouse.setStock(10);
    createWarehouseUseCase.create(warehouse);
  }
}
