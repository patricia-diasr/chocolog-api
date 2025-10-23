package com.chocolog.api.repository;

import com.chocolog.api.dto.response.reports.*;
import com.chocolog.api.model.Order;
import com.chocolog.api.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface ReportRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT 
            function('YEAR', o.creationDate),
            function('WEEK', o.creationDate),
            CAST(SUM(oi.quantity) as int)
        FROM Order o
        JOIN o.orderItems oi
        WHERE o.creationDate BETWEEN :startDate AND :endDate
        AND o.status != 'CANCELLED'
        AND o.active = true AND oi.active = true
        GROUP BY function('YEAR', o.creationDate), function('WEEK', o.creationDate)
        ORDER BY function('YEAR', o.creationDate), function('WEEK', o.creationDate)
    """)
    List<Object[]> getRawSalesByWeek(LocalDateTime startDate, LocalDateTime endDate);

    default List<SalesByPeriodDTO> getSalesByWeek(LocalDateTime startDate, LocalDateTime endDate) {
        return getRawSalesByWeek(startDate, endDate).stream()
                .map(arr -> {
                    String period = arr[0] + "-W" + arr[1];
                    Integer quantity = ((Number) arr[2]).intValue();
                    return new SalesByPeriodDTO(period, quantity);
                })
                .toList();
    }

    @Query(value = """
        SELECT 
            o.status, 
            COUNT(o.id) 
        FROM orders o
        WHERE o.creation_date BETWEEN :startDate AND :endDate
        AND o.active = true
        GROUP BY o.status
    """, nativeQuery = true)
        List<Object[]> getRawOrdersCountByStatus(LocalDateTime startDate, LocalDateTime endDate);

    default List<OrdersByStatusDTO> getOrdersCountByStatus(LocalDateTime startDate, LocalDateTime endDate) {
        return getRawOrdersCountByStatus(startDate, endDate).stream()
                .map(arr -> OrdersByStatusDTO.builder()
                        .status(OrderStatus.valueOf((String) arr[0]))
                        .count(((Number) arr[1]).longValue())
                        .build())
                .toList();
    }

    @Query(value = """
        SELECT 
            CASE WHEN oi.on_demand = TRUE THEN 'ON_DEMAND' ELSE 'STOCK' END, 
            SUM(oi.quantity)
        FROM order_items oi
        JOIN orders o ON o.id = oi.order_id
        WHERE o.creation_date BETWEEN :startDate AND :endDate
        AND o.status != 'CANCELLED'
        AND o.active = true AND oi.active = true
        GROUP BY CASE WHEN oi.on_demand = TRUE THEN 'ON_DEMAND' ELSE 'STOCK' END
    """, nativeQuery = true)
    List<Object[]> getRawOnDemandVsStock(LocalDateTime startDate, LocalDateTime endDate);

    default List<OnDemandVsStockDTO> getOnDemandVsStock(LocalDateTime startDate, LocalDateTime endDate) {
        return getRawOnDemandVsStock(startDate, endDate).stream()
                .map(arr -> OnDemandVsStockDTO.builder()
                        .type((String) arr[0])
                        .quantity(((Number) arr[1]).intValue())
                        .build())
                .toList();
    }

    @Query("""
        SELECT 
            function('YEAR', o.creationDate),
            function('WEEK', o.creationDate),
            SUM(c.totalAmount),
            SUM(p.paidAmount)
        FROM Order o
        JOIN o.charges c
        LEFT JOIN c.payments p
        WHERE o.creationDate BETWEEN :startDate AND :endDate
        AND o.active = true
        AND c.active = true
        AND o.status != 'CANCELLED'
        AND (p.active = true OR p.id IS NULL)
        GROUP BY function('YEAR', o.creationDate), function('WEEK', o.creationDate)
        ORDER BY function('YEAR', o.creationDate), function('WEEK', o.creationDate)
    """)
    List<Object[]> getRawRevenueVsReceivedByWeek(LocalDateTime startDate, LocalDateTime endDate);

    default List<RevenueVsReceivedDTO> getRevenueVsReceivedByWeek(LocalDateTime startDate, LocalDateTime endDate) {
        return getRawRevenueVsReceivedByWeek(startDate, endDate).stream()
                .map(arr -> {
                    String period = arr[0] + "-W" + arr[1];
                    BigDecimal revenue = (BigDecimal) arr[2];
                    BigDecimal received = (BigDecimal) arr[3];

                    return new RevenueVsReceivedDTO(
                            period,
                            revenue,
                            received != null ? received : BigDecimal.ZERO
                    );
                })
                .toList();
    }

    @Query("""
        SELECT 
            p.paymentMethod, 
            SUM(p.paidAmount)
        FROM Payment p
        WHERE p.paymentDate BETWEEN :startDate AND :endDate
        GROUP BY p.paymentMethod
    """)
    List<Object[]> getRawReceivedByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate);

    default List<ReceivedByPaymentMethodDTO> getReceivedByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate) {
        return getRawReceivedByPaymentMethod(startDate, endDate).stream()
                .map(arr -> new ReceivedByPaymentMethodDTO((String) arr[0], (BigDecimal) arr[1]))
                .toList();
    }

    @Query("""
        SELECT 
            SUM(oi.totalPrice - (oi.quantity * pp.costPrice))
        FROM OrderItem oi
        JOIN oi.order o
        JOIN ProductPrice pp ON pp.flavor = oi.flavor1 AND pp.size = oi.size
        WHERE o.creationDate BETWEEN :startDate AND :endDate
        AND o.status != 'CANCELLED'
    """)
    BigDecimal calculateEstimatedProfit(LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
        SELECT oi.flavor1.name, oi.size.name, CAST(SUM(oi.quantity) AS int)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.creationDate BETWEEN :startDate AND :endDate
        AND o.status != 'CANCELLED'
        AND oi.active = true
        AND o.active = true
        AND oi.onDemand = :onDemand
        GROUP BY oi.flavor1.name, oi.size.name
    """)
    List<Object[]> getRawTotalByFlavorAndSize(LocalDateTime startDate, LocalDateTime endDate, boolean onDemand);

    default List<TotalByFlavorAndSizeDTO> getTotalByFlavorAndSize(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawData = getRawTotalByFlavorAndSize(startDate, endDate, false);

        Map<String, List<SizeSalesDTO>> groupedData = rawData.stream()
                .collect(Collectors.groupingBy(
                        arr -> (String) arr[0],
                        Collectors.mapping(
                                arr -> new SizeSalesDTO((String) arr[1], (Integer) arr[2]),
                                Collectors.toList()
                        )
                ));

        return groupedData.entrySet().stream()
                .map(entry -> TotalByFlavorAndSizeDTO.builder()
                        .flavor(entry.getKey())
                        .sizeSales(entry.getValue())
                        .build())
                .toList();
    }
}
