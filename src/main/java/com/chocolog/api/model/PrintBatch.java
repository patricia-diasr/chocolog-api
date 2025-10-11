package com.chocolog.api.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "print_batches")
@SQLDelete(sql = "UPDATE print_batches SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class PrintBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "printed_by_employee_id")
    private Employee printedBy;

    private LocalDateTime createdAt;

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "printBatch")
    private List<PrintBatchItem> items;
}
