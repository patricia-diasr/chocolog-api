package com.chocolog.api.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "flavors")
public class Flavor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "flavor1")
    private List<OrderItem> flavor1Orders;

    @OneToMany(mappedBy = "flavor2")
    private List<OrderItem> flavor2Orders;

    @OneToMany(mappedBy = "flavor")
    private List<ProductPrice> productPrices;

    @OneToMany(mappedBy = "flavor")
    private List<Stock> stocks;

    @OneToMany(mappedBy = "flavor")
    private List<StockRecord> stockRecords;
}
