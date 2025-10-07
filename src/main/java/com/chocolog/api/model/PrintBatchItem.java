package com.chocolog.api.model;

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
public class PrintBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "print_batch_id")
    private PrintBatch printBatch;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;
}
