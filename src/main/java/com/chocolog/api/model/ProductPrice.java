package com.chocolog.api.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "product_prices")
@SQLDelete(sql = "UPDATE product_prices SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class ProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flavor_id")
    private Flavor flavor;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    private BigDecimal salePrice;
    private BigDecimal costPrice;

    @Builder.Default
    private boolean active = true;
}
