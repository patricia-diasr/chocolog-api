package com.chocolog.api.service;

import com.chocolog.api.dto.response.reports.*;
import com.chocolog.api.model.OrderStatus;
import com.chocolog.api.model.PeriodType;
import com.chocolog.api.repository.ChargeRepository;
import com.chocolog.api.repository.OrderItemRepository;
import com.chocolog.api.repository.PaymentRepository;
import com.chocolog.api.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para ReportService")
public class ReportServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ChargeRepository chargeRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<OrdersByStatusDTO> mockOrdersByStatus;
    private List<TotalByFlavorAndSizeDTO> mockTotalByFlavor;
    private List<OnDemandVsStockDTO> mockOnDemandVsStock;
    private List<SalesByPeriodDTO> mockSalesByDay;
    private List<SalesByPeriodDTO> mockSalesByWeek;
    private List<SalesByPeriodDTO> mockSalesByMonth;
    private List<RevenueVsReceivedDTO> mockRevenueByDay;
    private List<RevenueVsReceivedDTO> mockRevenueByWeek;
    private List<RevenueVsReceivedDTO> mockRevenueByMonth;
    private List<ReceivedByPaymentMethodDTO> mockReceivedByMethod;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

        mockOrdersByStatus = List.of(new OrdersByStatusDTO(OrderStatus.COMPLETED, 10L));
        mockTotalByFlavor = List.of(mock(TotalByFlavorAndSizeDTO.class));
        mockOnDemandVsStock = List.of(new OnDemandVsStockDTO("STOCK", 50));

        mockSalesByDay = List.of(new SalesByPeriodDTO("2025-01-01", 5));
        mockSalesByWeek = List.of(new SalesByPeriodDTO("2025-W1", 20));
        mockSalesByMonth = List.of(new SalesByPeriodDTO("2025-01", 100));

        mockRevenueByDay = List.of(new RevenueVsReceivedDTO("2025-01-01", BigDecimal.TEN, BigDecimal.ONE));
        mockRevenueByWeek = List.of(new RevenueVsReceivedDTO("2025-W1", BigDecimal.TEN, BigDecimal.ONE));
        mockRevenueByMonth = List.of(new RevenueVsReceivedDTO("2025-01", BigDecimal.TEN, BigDecimal.ONE));

        mockReceivedByMethod = List.of(new ReceivedByPaymentMethodDTO("PIX", BigDecimal.TEN));
    }

    private void mockKpiCalls(Integer sold, BigDecimal revenue, BigDecimal received, BigDecimal profit) {
        when(orderItemRepository.sumQuantityForOrders(startDate, endDate)).thenReturn(sold);
        when(chargeRepository.sumTotalAmountForOrders(startDate, endDate)).thenReturn(revenue);
        when(paymentRepository.sumPaidAmount(startDate, endDate)).thenReturn(received);
        when(reportRepository.calculateEstimatedProfit(startDate, endDate)).thenReturn(profit);
    }

    private void mockCommonReportCalls() {
        when(reportRepository.getOrdersCountByStatus(startDate, endDate)).thenReturn(mockOrdersByStatus);
        when(reportRepository.getTotalByFlavorAndSize(startDate, endDate)).thenReturn(mockTotalByFlavor);
        when(reportRepository.getOnDemandVsStock(startDate, endDate)).thenReturn(mockOnDemandVsStock);
        when(reportRepository.getReceivedByPaymentMethod(startDate, endDate)).thenReturn(mockReceivedByMethod);
    }

    // --- Testes para getDashboardReports ---

    @Test
    @DisplayName("Deve buscar relatórios por DIA quando PeriodType.DAY for fornecido")
    void getDashboardReports_ShouldFetchByDay_WhenPeriodTypeIsDay() {
        // Arrange
        mockKpiCalls(100, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE);
        mockCommonReportCalls();
        when(reportRepository.getSalesByDay(startDate, endDate)).thenReturn(mockSalesByDay);
        when(reportRepository.getRevenueVsReceivedByDay(startDate, endDate)).thenReturn(mockRevenueByDay);

        // Act
        ReportsDTO result = reportService.getDashboardReports(startDate, endDate, PeriodType.DAY);

        // Assert
        assertNotNull(result);
        assertEquals(mockSalesByDay, result.getSalesByPeriod());
        assertEquals(mockRevenueByDay, result.getFinancials().getRevenueVsReceivedByPeriod());

        verify(reportRepository, times(1)).getSalesByDay(startDate, endDate);
        verify(reportRepository, times(1)).getRevenueVsReceivedByDay(startDate, endDate);

        verify(reportRepository, never()).getSalesByWeek(any(), any());
        verify(reportRepository, never()).getRevenueVsReceivedByMonth(any(), any());
    }

    @Test
    @DisplayName("Deve buscar relatórios por SEMANA quando PeriodType.WEEK for fornecido")
    void getDashboardReports_ShouldFetchByWeek_WhenPeriodTypeIsWeek() {
        // Arrange
        mockKpiCalls(100, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE);
        mockCommonReportCalls();
        when(reportRepository.getSalesByWeek(startDate, endDate)).thenReturn(mockSalesByWeek);
        when(reportRepository.getRevenueVsReceivedByWeek(startDate, endDate)).thenReturn(mockRevenueByWeek);

        // Act
        ReportsDTO result = reportService.getDashboardReports(startDate, endDate, PeriodType.WEEK);

        // Assert
        assertNotNull(result);
        assertEquals(mockSalesByWeek, result.getSalesByPeriod());
        assertEquals(mockRevenueByWeek, result.getFinancials().getRevenueVsReceivedByPeriod());

        verify(reportRepository, times(1)).getSalesByWeek(startDate, endDate);
        verify(reportRepository, times(1)).getRevenueVsReceivedByWeek(startDate, endDate);
        verify(reportRepository, never()).getSalesByDay(any(), any());
    }

    @Test
    @DisplayName("Deve buscar relatórios por MÊS quando PeriodType.MONTH for fornecido")
    void getDashboardReports_ShouldFetchByMonth_WhenPeriodTypeIsMonth() {
        // Arrange
        mockKpiCalls(100, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE);
        mockCommonReportCalls();
        when(reportRepository.getSalesByMonth(startDate, endDate)).thenReturn(mockSalesByMonth);
        when(reportRepository.getRevenueVsReceivedByMonth(startDate, endDate)).thenReturn(mockRevenueByMonth);

        // Act
        ReportsDTO result = reportService.getDashboardReports(startDate, endDate, PeriodType.MONTH);

        // Assert
        assertNotNull(result);
        assertEquals(mockSalesByMonth, result.getSalesByPeriod());
        assertEquals(mockRevenueByMonth, result.getFinancials().getRevenueVsReceivedByPeriod());

        verify(reportRepository, times(1)).getSalesByMonth(startDate, endDate);
        verify(reportRepository, times(1)).getRevenueVsReceivedByMonth(startDate, endDate);
        verify(reportRepository, never()).getSalesByWeek(any(), any());
    }

    @Test
    @DisplayName("Deve preencher KPIs com zero quando repositórios retornarem nulo")
    void getDashboardReports_ShouldSetKpisToZero_WhenRepositoriesReturnNull() {
        // Arrange
        mockKpiCalls(null, null, null, null);
        mockCommonReportCalls();
        
        when(reportRepository.getSalesByWeek(startDate, endDate)).thenReturn(Collections.emptyList());
        when(reportRepository.getRevenueVsReceivedByWeek(startDate, endDate)).thenReturn(Collections.emptyList());

        // Act
        ReportsDTO result = reportService.getDashboardReports(startDate, endDate, PeriodType.WEEK);

        // Assert
        assertNotNull(result);
        KpisDTO kpis = result.getKpis();
        assertNotNull(kpis);

        assertEquals(0, kpis.getTotalSold());
        assertEquals(BigDecimal.ZERO, kpis.getTotalRevenue());
        assertEquals(BigDecimal.ZERO, kpis.getTotalReceived());
        assertEquals(BigDecimal.ZERO, kpis.getEstimatedProfit());
    }

    @Test
    @DisplayName("Deve montar o ReportsDTO completo corretamente")
    void getDashboardReports_ShouldBuildFullReportDTO_WhenAllDataIsPresent() {
        // Arrange
        mockKpiCalls(100, new BigDecimal("1000.00"), new BigDecimal("800.00"), new BigDecimal("300.00"));
        mockCommonReportCalls();
        when(reportRepository.getSalesByWeek(startDate, endDate)).thenReturn(mockSalesByWeek);
        when(reportRepository.getRevenueVsReceivedByWeek(startDate, endDate)).thenReturn(mockRevenueByWeek);

        // Act
        ReportsDTO result = reportService.getDashboardReports(startDate, endDate, PeriodType.WEEK);

        // Assert
        assertEquals(100, result.getKpis().getTotalSold());
        assertEquals(new BigDecimal("1000.00"), result.getKpis().getTotalRevenue());

        assertEquals(mockOrdersByStatus, result.getOrdersByStatus());
        assertEquals(mockTotalByFlavor, result.getTotalByFlavorAndSize());
        assertEquals(mockOnDemandVsStock, result.getOnDemandVsStock());
        assertEquals(mockSalesByWeek, result.getSalesByPeriod());

        assertNotNull(result.getFinancials());
        assertEquals(mockRevenueByWeek, result.getFinancials().getRevenueVsReceivedByPeriod());
        assertEquals(mockReceivedByMethod, result.getFinancials().getReceivedByPaymentMethod());

        assertEquals(startDate.toString(), result.getPeriodStart());
        assertEquals(endDate.toString(), result.getPeriodEnd());
    }
}