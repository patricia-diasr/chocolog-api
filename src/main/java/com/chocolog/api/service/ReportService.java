package com.chocolog.api.service;

import com.chocolog.api.dto.response.reports.*;
import com.chocolog.api.model.PeriodType;
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

    public ReportsDTO getDashboardReports(LocalDateTime startDate, LocalDateTime endDate, PeriodType periodType) {
        String start = startDate.toString();
        String end = endDate.toString();

        KpisDTO kpis = calculateKpis(startDate, endDate);
        List<OrdersByStatusDTO> ordersByStatus = reportRepository.getOrdersCountByStatus(startDate, endDate);
        List<TotalByFlavorAndSizeDTO> totalByFlavorAndSize = reportRepository.getTotalByFlavorAndSize(startDate, endDate);
        List<OnDemandVsStockDTO> onDemandVsStock = reportRepository.getOnDemandVsStock(startDate, endDate);

        List<SalesByPeriodDTO> salesByPeriod;
        switch (periodType) {
            case DAY:
                salesByPeriod = reportRepository.getSalesByDay(startDate, endDate);
                break;
            case MONTH:
                salesByPeriod = reportRepository.getSalesByMonth(startDate, endDate);
                break;
            case WEEK:
            default:
                salesByPeriod = reportRepository.getSalesByWeek(startDate, endDate);
                break;
        }

        List<RevenueVsReceivedDTO> revenueVsReceived;
        switch (periodType) {
            case DAY:
                revenueVsReceived = reportRepository.getRevenueVsReceivedByDay(startDate, endDate);
                break;
            case MONTH:
                revenueVsReceived = reportRepository.getRevenueVsReceivedByMonth(startDate, endDate);
                break;
            case WEEK:
            default:
                revenueVsReceived = reportRepository.getRevenueVsReceivedByWeek(startDate, endDate);
                break;
        }

        FinancialsDTO financials = FinancialsDTO.builder()
                .revenueVsReceivedByPeriod(revenueVsReceived)
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