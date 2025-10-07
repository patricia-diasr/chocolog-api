package com.chocolog.api.model;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "charges")
public class Charge {

    public enum Status {
        PAID,
        UNPAID,
        PARTIAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private BigDecimal totalAmount;
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "charge")
    private List<Payment> payments;
}
