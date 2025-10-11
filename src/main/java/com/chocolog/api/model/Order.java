package com.chocolog.api.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
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
@SQLDelete(sql = "UPDATE orders SET active = false WHERE id = ?")
@Where(clause = "active = true")
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

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order")
    private List<Charge> charges;
}
