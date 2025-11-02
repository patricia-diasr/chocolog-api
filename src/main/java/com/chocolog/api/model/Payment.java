package com.chocolog.api.model;

import com.chocolog.api.audit.AuditListener;
import com.chocolog.api.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@EntityListeners(AuditListener.class)
public class Payment implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "charge_id")
    @JsonBackReference("charge-payments")
    private Charge charge;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonBackReference("employee-payments")
    private Employee employee;

    private BigDecimal paidAmount;
    private LocalDateTime paymentDate;
    private String paymentMethod;

    @Builder.Default
    private boolean active = true;

    @Override
    public Long getId() {
        return this.id;
    }
}