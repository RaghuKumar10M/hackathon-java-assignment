package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {
    // Validation 1: Warehouse must exist
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode());
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + newWarehouse.getBusinessUnitCode() + "' does not exist");
    }

    // Validation 2: Warehouse must not be archived
    if (existing.getArchivedAt() != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + newWarehouse.getBusinessUnitCode() + "' is archived and cannot be replaced");
    }

    // Validation 3: Location must be valid
    Location location = locationResolver.resolveByIdentifier(newWarehouse.getLocation());
    if (location == null) {
      throw new IllegalArgumentException(
          "Location '" + newWarehouse.getLocation() + "' is not valid");
    }

    // Validation 4: Capacity validation
    // - Capacity cannot exceed location's max capacity
    if (newWarehouse.getCapacity() > location.maxCapacity()) {
      throw new IllegalArgumentException(
          "Warehouse capacity (" + newWarehouse.getCapacity() +
          ") exceeds location max capacity (" + location.maxCapacity() + ")");
    }

    // - Stock cannot exceed capacity
    if (newWarehouse.getStock() > newWarehouse.getCapacity()) {
      throw new IllegalArgumentException(
          "Warehouse stock (" + newWarehouse.getStock() +
          ") exceeds warehouse capacity (" + newWarehouse.getCapacity() + ")");
    }

    // Update warehouse fields (preserve createdAt, businessUnitCode, archivedAt)
    existing.setLocation(newWarehouse.getLocation());
    existing.setCapacity(newWarehouse.getCapacity());
    existing.setStock(newWarehouse.getStock());

    // Update the warehouse
    warehouseStore.update(existing);
  }
}
