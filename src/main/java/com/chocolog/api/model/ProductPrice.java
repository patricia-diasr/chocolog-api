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
@ToString
@Entity
@Table(name = "product_prices")
@SQLDelete(sql = "UPDATE product_prices SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class ProductPrice implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flavor_id")
    @JsonBackReference("flavor-prices")
    private Flavor flavor;

    @ManyToOne
    @JoinColumn(name = "size_id")
    @JsonBackReference("size-prices")
    private Size size;

    private BigDecimal salePrice;
    private BigDecimal costPrice;

    @Builder.Default
    private boolean active = true;

    @Override
    public Long getId() {
        return this.id;
    }
}
