package com.chocolog.api.model;

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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "flavor1_id")
    private Flavor flavor1;

    @ManyToOne
    @JoinColumn(name = "flavor2_id", nullable = true)
    private Flavor flavor2;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean onDemand;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
