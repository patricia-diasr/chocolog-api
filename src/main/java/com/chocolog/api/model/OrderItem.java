package com.chocolog.api.model;

import com.chocolog.api.audit.AuditListener;
import com.chocolog.api.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Entity
@Table(name = "order_items")
@SQLDelete(sql = "UPDATE order_items SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class OrderItem implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference("order-orderitems")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "size_id")
    @JsonBackReference("size-orderitems")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "flavor1_id")
    @JsonBackReference("flavor1-orderitem")
    private Flavor flavor1;

    @ManyToOne
    @JoinColumn(name = "flavor2_id", nullable = true)
    @JsonBackReference("flavor2-orderitem")
    private Flavor flavor2;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean onDemand;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private boolean active = true;

    @Override
    public Long getId() {
        return this.id;
    }
}
