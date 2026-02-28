package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.setBusinessUnitCode(warehouse.getBusinessUnitCode());
    dbWarehouse.setLocation(warehouse.getLocation());
    dbWarehouse.setCapacity(warehouse.getCapacity());
    dbWarehouse.setStock(warehouse.getStock());
    dbWarehouse.setCreatedAt(warehouse.getCreatedAt());
    dbWarehouse.setArchivedAt(warehouse.getArchivedAt());
    dbWarehouse.setVersion(warehouse.getVersion());

    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    // Find the existing DbWarehouse entity to update
    DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.getBusinessUnitCode()).firstResult();

    if (dbWarehouse != null) {
      // Update fields
      dbWarehouse.setLocation(warehouse.getLocation());
      dbWarehouse.setCapacity(warehouse.getCapacity());
      dbWarehouse.setStock(warehouse.getStock());
      dbWarehouse.setArchivedAt(warehouse.getArchivedAt());
      // Note: version field is handled by JPA @Version annotation

      // Using merge() respects optimistic locking (@Version field)
      // If another transaction modified this entity, OptimisticLockException will be thrown
      getEntityManager().merge(dbWarehouse);
      getEntityManager().flush();
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  @Transactional
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}
