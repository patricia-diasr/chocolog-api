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
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private Boolean isReseller;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
}
