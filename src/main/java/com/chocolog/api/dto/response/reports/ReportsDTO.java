package com.chocolog.api.dto.response.reports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportsDTO {

    private String periodStart;
    private String periodEnd;
    private KpisDTO kpis;
    private List<SalesByPeriodDTO> salesByPeriod;
    private List<OrdersByStatusDTO> ordersByStatus;
    private List<TotalByFlavorAndSizeDTO> totalByFlavorAndSize;
    private List<OnDemandVsStockDTO> onDemandVsStock;
    private FinancialsDTO financials;

}