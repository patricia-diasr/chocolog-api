package com.chocolog.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentPatchRequestDTO {

    private BigDecimal paidAmount;
    private String paymentMethod;
    private LocalDateTime paymentDate;

}