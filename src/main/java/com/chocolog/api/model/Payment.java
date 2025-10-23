package com.chocolog.api.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
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
@SQLDelete(sql = "UPDATE payments SET active = false WHERE id = ?")
@Where(clause = "active = true")
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

    @Builder.Default
    private boolean active = true;
}