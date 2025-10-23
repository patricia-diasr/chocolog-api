package com.chocolog.api.repository;

import com.chocolog.api.model.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    Optional<Charge> findByOrderId(Long orderId);

    @Query("""
        SELECT SUM(c.totalAmount)
        FROM Charge c
        JOIN c.order o
        WHERE o.creationDate BETWEEN :startDate AND :endDate
        AND o.status != 'CANCELLED'
        AND c.active = true
        AND o.active = true
    """)
    BigDecimal sumTotalAmountForOrders(LocalDateTime startDate, LocalDateTime endDate);
}