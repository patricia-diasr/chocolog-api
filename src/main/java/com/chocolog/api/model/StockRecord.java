package com.chocolog.api.model;

import com.chocolog.api.audit.AuditListener;
import com.chocolog.api.audit.Auditable;
import com.chocolog.api.model.StockMovement;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "stock_records")
@SQLDelete(sql = "UPDATE stock_records SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class StockRecord implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flavor_id")
    @JsonBackReference("flavor-stockrecords")
    private Flavor flavor;

    @ManyToOne
    @JoinColumn(name = "size_id")
    @JsonBackReference("size-stockrecords")
    private Size size;

    private Integer quantity;

    private LocalDateTime productionDate;
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING) 
    private StockMovement movementType;

    @Builder.Default
    private boolean active = true;

    @Override
    public Long getId() {
        return this.id;
    }
}
