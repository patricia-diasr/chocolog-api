package com.chocolog.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentPatchRequestDTO {

    @NotNull
    private BigDecimal paidAmount;

    @NotNull
    private String paymentMethod;

}