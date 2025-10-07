package com.chocolog.api.model;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "charge_id")
    private Charge charge;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private BigDecimal paidAmount;
    private LocalDateTime paymentDate;
    private String paymentMethod;
}
