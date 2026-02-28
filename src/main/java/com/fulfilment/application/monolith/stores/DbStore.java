package com.fulfilment.application.monolith.stores;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Cacheable
@Data
@NoArgsConstructor
public class DbStore {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "store_seq_gen")
  @SequenceGenerator(name = "store_seq_gen", sequenceName = "store_seq", allocationSize = 1)
  private Long id;

  @Column(length = 40, unique = true)
  private String name;


  private int quantityProductsInStock;


  public DbStore(String name) {
    this.name = name;
  }
}
