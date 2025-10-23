package com.chocolog.api.repository;

import com.chocolog.api.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Optional<OrderItem> findByIdAndOrder_Id(Long itemId, Long orderId);

    @Query("""
        SELECT CAST(SUM(oi.quantity) AS int)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.creationDate BETWEEN :startDate AND :endDate
        AND o.status != 'CANCELLED'
        AND o.active = true
        AND oi.active = true
    """)
    Integer sumQuantityForOrders(LocalDateTime startDate, LocalDateTime endDate);

}