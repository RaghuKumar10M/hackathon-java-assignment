package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
@Cacheable
@NoArgsConstructor
@Data
public class DbWarehouse {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_seq_gen")
  @SequenceGenerator(name = "warehouse_seq_gen", sequenceName = "warehouse_seq", allocationSize = 1)
  private Long id;

  @Version
  private Long version;

  @Column(unique = true, nullable = false)
  private String businessUnitCode;

  private String location;

  private Integer capacity;

  private Integer stock;

  private LocalDateTime createdAt;

  private LocalDateTime archivedAt;


  public Warehouse toWarehouse() {
    var warehouse = new Warehouse();
    warehouse.setBusinessUnitCode(this.businessUnitCode);
    warehouse.setLocation(this.location);
    warehouse.setCapacity(this.capacity);
    warehouse.setStock(this.stock);
    warehouse.setCreatedAt(this.createdAt);
    warehouse.setArchivedAt(this.archivedAt);
    warehouse.setVersion(this.version);
    return warehouse;
  }
}
