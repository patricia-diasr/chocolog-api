package com.chocolog.api.model;

import com.chocolog.api.audit.AuditListener;
import com.chocolog.api.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import jakarta.persistence.*;

import java.util.ArrayList;
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
@SQLRestriction("active = true")
@EntityListeners(AuditListener.class)
public class Employee implements Auditable {

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

    @Builder.Default
    @OneToMany(mappedBy = "employee")
    @JsonManagedReference("employee-orders")
    private List<Order> orders = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "employee")
    @JsonManagedReference("employee-payments")
    private List<Payment> payments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "printedBy")
    @JsonManagedReference("employee-printbatches")
    private List<PrintBatch> printBatches = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "employee")
    @JsonManagedReference("employee-audits")
    private List<Audit> audits = new ArrayList<>();

    @Override
    public Long getId() {
        return this.id;
    }
}
