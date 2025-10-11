package com.chocolog.api.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
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
@SQLDelete(sql = "UPDATE sizes SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "size")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "size")
    private List<ProductPrice> productPrices;

    @OneToMany(mappedBy = "size")
    private List<Stock> stocks;

    @OneToMany(mappedBy = "size")
    private List<StockRecord> stockRecords;
}
