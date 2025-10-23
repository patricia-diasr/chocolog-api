package com.chocolog.api.dto.request;

import com.chocolog.api.validation.FutureOrPresentDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PaymentRequestDTO {

    @NotNull(message = "Employee ID cannot be null")
    private Long employeeId;

    @NotNull
    private BigDecimal paidAmount;

    @NotEmpty
    private String paymentMethod;

    @NotNull(message = "Payment date cannot be null")
    private LocalDateTime paymentDate;

}