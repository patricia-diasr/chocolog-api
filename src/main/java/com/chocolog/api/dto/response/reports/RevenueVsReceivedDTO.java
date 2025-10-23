package com.chocolog.api.dto.response.reports;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueVsReceivedDTO {

    private String period;
    private BigDecimal revenue;
    private BigDecimal received;

}