package com.chocolog.api.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
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
@SQLDelete(sql = "UPDATE charges SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class Charge {

    public enum Status {
        PAID,
        UNPAID,
        PARTIAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private BigDecimal subTotalAmount;
    private BigDecimal discount;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "charge")
    private List<Payment> payments;
}
