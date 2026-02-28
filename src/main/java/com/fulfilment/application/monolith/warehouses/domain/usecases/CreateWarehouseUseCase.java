package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    // Validation 1: Business unit code must be unique
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.getBusinessUnitCode());
    if (existing != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.getBusinessUnitCode() + "' already exists");
    }

    // Validation 2: Location must be valid (must exist)
    Location location = locationResolver.resolveByIdentifier(warehouse.getLocation());
    if (location == null) {
      throw new IllegalArgumentException(
          "Location '" + warehouse.getLocation() + "' is not valid");
    }

    // Validation 3: Capacity validation
    // - Capacity cannot exceed location's max capacity
    if (warehouse.getCapacity() > location.maxCapacity()) {
      throw new IllegalArgumentException(
          "Warehouse capacity (" + warehouse.getCapacity() +
          ") exceeds location max capacity (" + location.maxCapacity() + ")");
    }

    // - Stock cannot exceed capacity
    if (warehouse.getStock() > warehouse.getCapacity()) {
      throw new IllegalArgumentException(
          "Warehouse stock (" + warehouse.getStock() +
          ") exceeds warehouse capacity (" + warehouse.getCapacity() + ")");
    }

    // Set creation timestamp
    warehouse.setCreatedAt(java.time.LocalDateTime.now());

    // All validations passed, create the warehouse
    warehouseStore.create(warehouse);
  }
}
