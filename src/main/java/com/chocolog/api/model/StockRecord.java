package com.chocolog.api.model;

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
public class StockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "flavor_id")
    private Flavor flavor;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    private LocalDateTime productionDate;
    private LocalDateTime expirationDate;
    private String status;

    @Builder.Default
    private boolean active = true;
}
