package com.fulfilment.application.monolith.products;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Cacheable
@NoArgsConstructor
@Data
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
  @SequenceGenerator(name = "product_seq_gen", sequenceName = "product_seq", allocationSize = 1)
  private Long id;

  @Column(length = 40, unique = true)
  private String name;

  @Column(nullable = true)
  private String description;

  @Column(precision = 10, scale = 2, nullable = true)
  private BigDecimal price;

  private int stock;


  public Product(String name) {
    this.name = name;
  }
}
