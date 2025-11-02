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
@Table(name = "print_batch_items")
@SQLDelete(sql = "UPDATE print_batch_items SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class PrintBatchItem implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "print_batch_id")
    @JsonBackReference("batch-items")
    private PrintBatch printBatch;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Builder.Default
    private boolean active = true;

    @Override
    public Long getId() {
        return this.id;
    }
}
