package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseStore warehouseStore;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits(@QueryParam("excludeArchived") @DefaultValue("false") Boolean excludeArchived) {
    return warehouseStore.getAll().stream()
            .filter(w -> {
                if (excludeArchived != null && excludeArchived) {
                    return w.getArchivedAt() == null; // Only include non-archived warehouses
                }
                return true; // Include all warehouses if excludeArchived is false
            }) // Filter out archived warehouses if includeArchived is false
            .map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.setBusinessUnitCode(data.getBusinessUnitCode());
    domainWarehouse.setLocation(data.getLocation());
    domainWarehouse.setCapacity(data.getCapacity());
    domainWarehouse.setStock(data.getStock() != null ? data.getStock() : 0);

    try {
      // Create warehouse through use case (includes validations)
      createWarehouseOperation.create(domainWarehouse);
      
      // Return the created warehouse
      return toWarehouseResponse(domainWarehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    // Find warehouse by business unit code
    var domainWarehouse = warehouseStore.findByBusinessUnitCode(id);
    
    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }
    
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    // Find warehouse by business unit code
    var domainWarehouse = warehouseStore.findByBusinessUnitCode(id);

    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    try {
      // Archive warehouse through use case (includes validations)
      archiveWarehouseOperation.archive(domainWarehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.setBusinessUnitCode(businessUnitCode); // Use businessUnitCode from path
    domainWarehouse.setLocation(data.getLocation());
    domainWarehouse.setCapacity(data.getCapacity());
    domainWarehouse.setStock(data.getStock() != null ? data.getStock() : 0);

    try {
      // Replace warehouse through use case (includes validations)
      replaceWarehouseOperation.replace(domainWarehouse);

      // Return the updated warehouse
      var updated = warehouseStore.findByBusinessUnitCode(businessUnitCode);
      return toWarehouseResponse(updated);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.getBusinessUnitCode());
    response.setLocation(warehouse.getLocation());
    response.setCapacity(warehouse.getCapacity());
    response.setStock(warehouse.getStock());

    return response;
  }
}
