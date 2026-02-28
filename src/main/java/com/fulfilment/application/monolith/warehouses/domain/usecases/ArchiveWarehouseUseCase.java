package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  @Transactional
  public void archive(Warehouse warehouse) {
    // Validation 1: Warehouse must exist
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.getBusinessUnitCode());
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.getBusinessUnitCode() + "' does not exist");
    }

    // Validation 2: Warehouse must not already be archived
    if (existing.getArchivedAt() != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.getBusinessUnitCode() + "' is already archived");
    }

    // Set archive timestamp
    existing.setArchivedAt(java.time.LocalDateTime.now());

    // Update the warehouse
    warehouseStore.update(existing);
  }
}
