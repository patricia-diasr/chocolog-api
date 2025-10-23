package com.chocolog.api.dto.response.reports;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedByPaymentMethodDTO {

    private String method;
    private BigDecimal amount;

}