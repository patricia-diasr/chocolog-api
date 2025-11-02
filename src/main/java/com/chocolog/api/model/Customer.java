package com.chocolog.api.model;

import com.chocolog.api.audit.AuditListener;
import com.chocolog.api.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditListener.class)
public class Customer implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private Boolean isReseller;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @OneToMany(mappedBy = "customer")
    @JsonManagedReference("customer-orders")
    private List<Order> orders = new ArrayList<>();

    @Override
    public Long getId() {
        return this.id;
    }
}
