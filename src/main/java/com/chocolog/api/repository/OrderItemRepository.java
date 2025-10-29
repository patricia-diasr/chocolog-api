package com.chocolog.api.repository;

import com.chocolog.api.dto.response.OrderItemResponseDTO;
import com.chocolog.api.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("""
        SELECT new com.chocolog.api.dto.response.OrderItemResponseDTO(
            oi.id,
            oi.order.id,
            oi.size.id,
            oi.size.name,
            oi.flavor1.id,
            oi.flavor1.name,
            f2.id,   /* Use o alias f2 */
            f2.name, /* Use o alias f2 */
            oi.quantity,
            oi.unitPrice,
            oi.totalPrice,
            oi.onDemand,
            (EXISTS (SELECT 1 FROM PrintBatchItem pbi WHERE pbi.orderItem = oi)),
            CAST(oi.status AS string),
            oi.notes
        )
        FROM OrderItem oi
        LEFT JOIN oi.flavor2 f2 /* Adicione este LEFT JOIN explícito */
    """)
    List<OrderItemResponseDTO> findAllAsDTO();

    @Query("""
        SELECT new com.chocolog.api.dto.response.OrderItemResponseDTO(
            oi.id,
            oi.order.id,
            oi.size.id,
            oi.size.name,
            oi.flavor1.id,
            oi.flavor1.name,
            f2.id,   /* Use o alias f2 */
            f2.name, /* Use o alias f2 */
            oi.quantity,
            oi.unitPrice,
            oi.totalPrice,
            oi.onDemand,
            (EXISTS (SELECT 1 FROM PrintBatchItem pbi WHERE pbi.orderItem = oi)),
            CAST(oi.status AS string),
            oi.notes
        )
        FROM OrderItem oi
        LEFT JOIN oi.flavor2 f2 /* Adicione este LEFT JOIN explícito */
        WHERE oi.onDemand = true
    """)
    List<OrderItemResponseDTO> findAllAsDTOByOnDemandTrue();
}