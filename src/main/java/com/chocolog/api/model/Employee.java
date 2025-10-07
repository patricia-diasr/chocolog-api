package com.chocolog.api.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String login;
    private String passwordHash;
    private String role;

    @OneToMany(mappedBy = "employee")
    private List<Order> orders;

    @OneToMany(mappedBy = "employee")
    private List<Payment> payments;

    @OneToMany(mappedBy = "printedBy")
    private List<PrintBatch> printBatches;

    @OneToMany(mappedBy = "employee")
    private List<Audit> audits;
}
