package com.chocolog.api.model;

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
public class PrintBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "printed_by_employee_id")
    private Employee printedBy;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "printBatch")
    private List<PrintBatchItem> items;
}
