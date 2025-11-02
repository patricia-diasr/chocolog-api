package com.chocolog.api.model;

import com.chocolog.api.audit.AuditListener;
import com.chocolog.api.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "stock")
@SQLDelete(sql = "UPDATE stock SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class Stock implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flavor_id")
    @JsonBackReference("flavor-stocks")
    private Flavor flavor;

    @ManyToOne
    @JoinColumn(name = "size_id")
    @JsonBackReference("size-stocks")
    private Size size;

    private Integer totalQuantity;
    private Integer remainingQuantity;

    @Builder.Default
    private boolean active = true;

    @Override
    public Long getId() {
        return this.id;
    }
}
