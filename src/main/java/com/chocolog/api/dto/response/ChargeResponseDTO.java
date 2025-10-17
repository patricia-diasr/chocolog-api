package com.chocolog.api.dto.response;

import com.chocolog.api.model.Charge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChargeResponseDTO {

    private final Long id;
    private final Long orderId;
    private final BigDecimal subTotalAmount;
    private final BigDecimal discount;
    private final BigDecimal totalAmount;
    private final Charge.Status status;
    private final List<PaymentResponseDTO> payments;
}