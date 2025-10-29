package com.chocolog.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponseDTO {

    private final Long id;
    private final Long orderId;
    private final Long sizeId;
    private final String sizeName;
    private final Long flavor1Id;
    private final String flavor1Name;
    private final Long flavor2Id;
    private final String flavor2Name;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal totalPrice;
    private final Boolean onDemand;
    private final Boolean isPrinted;
    private final String status;
    private final String notes;

}