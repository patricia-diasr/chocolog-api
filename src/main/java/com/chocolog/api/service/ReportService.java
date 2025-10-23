package com.chocolog.api.service;

import com.chocolog.api.dto.response.reports.*;
import com.chocolog.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final OrderItemRepository orderItemRepository;
    private final ChargeRepository chargeRepository;
    private final PaymentRepository paymentRepository;
    private final ReportRepository reportRepository;

    public ReportsDTO getDashboardReports(LocalDateTime startDate, LocalDateTime endDate) {
        String start = startDate.toString();
        String end = endDate.toString();

        KpisDTO kpis = calculateKpis(startDate, endDate);
        List<SalesByPeriodDTO> salesByPeriod = reportRepository.getSalesByWeek(startDate, endDate);
        List<OrdersByStatusDTO> ordersByStatus = reportRepository.getOrdersCountByStatus(startDate, endDate);
        List<TotalByFlavorAndSizeDTO> totalByFlavorAndSize = reportRepository.getTotalByFlavorAndSize(startDate, endDate);
        List<OnDemandVsStockDTO> onDemandVsStock = reportRepository.getOnDemandVsStock(startDate, endDate);

        FinancialsDTO financials = FinancialsDTO.builder()
                .revenueVsReceivedByPeriod(reportRepository.getRevenueVsReceivedByWeek(startDate, endDate))
                .receivedByPaymentMethod(reportRepository.getReceivedByPaymentMethod(startDate, endDate))
                .build();

        return ReportsDTO.builder()
                .periodStart(start)
                .periodEnd(end)
                .kpis(kpis)
                .salesByPeriod(salesByPeriod)
                .ordersByStatus(ordersByStatus)
                .totalByFlavorAndSize(totalByFlavorAndSize)
                .onDemandVsStock(onDemandVsStock)
                .financials(financials)
                .build();
    }

    private KpisDTO calculateKpis(LocalDateTime startDate, LocalDateTime endDate) {
        Integer totalSold = orderItemRepository.sumQuantityForOrders(startDate, endDate);

        BigDecimal totalRevenue = chargeRepository.sumTotalAmountForOrders(startDate, endDate);
        BigDecimal totalReceived = paymentRepository.sumPaidAmount(startDate, endDate);
        BigDecimal estimatedProfit = reportRepository.calculateEstimatedProfit(startDate, endDate);

        return KpisDTO.builder()
                .totalSold(totalSold != null ? totalSold : 0)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalReceived(totalReceived != null ? totalReceived : BigDecimal.ZERO)
                .estimatedProfit(estimatedProfit != null ? estimatedProfit : BigDecimal.ZERO)
                .build();
    }
}