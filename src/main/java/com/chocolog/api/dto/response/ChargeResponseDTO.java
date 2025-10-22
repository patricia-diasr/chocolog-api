package com.chocolog.api.dto.response;

import com.chocolog.api.model.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChargeResponseDTO {

    private final Long id;
    private final Long orderId;
    private final BigDecimal subtotalAmount;
    private final BigDecimal discount;
    private final BigDecimal totalAmount;
    private final ChargeStatus status;
    private final LocalDateTime date;
    private final BigDecimal amountDue;
    private final List<PaymentResponseDTO> payments;

}