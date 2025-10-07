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
@Table(name = "sizes")
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "size")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "size")
    private List<ProductPrice> productPrices;

    @OneToMany(mappedBy = "size")
    private List<Stock> stocks;

    @OneToMany(mappedBy = "size")
    private List<StockRecord> stockRecords;
}
