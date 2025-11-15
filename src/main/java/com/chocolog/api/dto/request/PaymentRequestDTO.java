package com.chocolog.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentRequestDTO {

    @NotNull
    private BigDecimal paidAmount;

    @NotEmpty
    private String paymentMethod;

    @NotNull(message = "Payment date cannot be null")
    private LocalDateTime paymentDate;

}