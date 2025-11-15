package com.chocolog.api.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentPatchRequestDTO {

    private BigDecimal paidAmount;
    private String paymentMethod;
    private LocalDateTime paymentDate;

}