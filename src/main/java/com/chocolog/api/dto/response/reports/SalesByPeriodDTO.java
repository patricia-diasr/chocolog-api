package com.chocolog.api.dto.response.reports;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesByPeriodDTO {

    private String period;
    private Integer totalSold;

}