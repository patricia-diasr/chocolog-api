package com.chocolog.api.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
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
@SQLDelete(sql = "UPDATE employees SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String login;
    
    private String passwordHash;

    @Enumerated(EnumType.STRING) 
    private Role role;

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "employee")
    private List<Order> orders;

    @OneToMany(mappedBy = "employee")
    private List<Payment> payments;

    @OneToMany(mappedBy = "printedBy")
    private List<PrintBatch> printBatches;

    @OneToMany(mappedBy = "employee")
    private List<Audit> audits;
}
