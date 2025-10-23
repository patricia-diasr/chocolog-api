package com.chocolog.api.repository;

import com.chocolog.api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdAndChargeId(Long id, Long chargeId);

    @Query("""
        SELECT SUM(p.paidAmount)
        FROM Payment p
        WHERE p.paymentDate BETWEEN :startDate AND :endDate
        AND p.active = true
    """)
    BigDecimal sumPaidAmount(LocalDateTime startDate, LocalDateTime endDate);
}