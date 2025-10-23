package com.chocolog.api.dto.response.reports;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpisDTO {

    private Integer totalSold;
    private BigDecimal totalRevenue;
    private BigDecimal totalReceived;
    private BigDecimal estimatedProfit;

}