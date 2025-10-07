package com.chocolog.api.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDateTime creationDate;
    private LocalDate expectedPickupDate;
    private LocalDateTime pickupDate;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order")
    private List<Charge> charges;
}
