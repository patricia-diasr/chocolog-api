package com.chocolog.api.model;

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
@Table(name = "audit")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String entityName;
    private Long entityId;
    private String action;

    @Column(columnDefinition = "TEXT")
    private String changedData;

    private LocalDateTime createdAt;
}
