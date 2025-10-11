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
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class Customer {

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

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
}
