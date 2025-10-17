package com.chocolog.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponseDTO {

    private final Long id;
    private final Long chargeId;
    private final Long employeeId;
    private final BigDecimal paidAmount;
    private final LocalDateTime paymentDate;
    private final String paymentMethod;

}