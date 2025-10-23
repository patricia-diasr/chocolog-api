package com.chocolog.api.dto.response.reports;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnDemandVsStockDTO {

    private String type;
    private Integer quantity;

}