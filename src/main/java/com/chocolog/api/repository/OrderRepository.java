package com.chocolog.api.repository;

import com.chocolog.api.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    List<Order> findByExpectedPickupDateBetweenOrderByExpectedPickupDate(
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}