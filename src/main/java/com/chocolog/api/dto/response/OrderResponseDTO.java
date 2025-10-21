package com.chocolog.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponseDTO {

    private final Long id;
    private final Long customerId;
    private final Long employeeId;
    private final LocalDateTime creationDate;
    private final LocalDateTime expectedPickupDate;
    private final LocalDateTime pickupDate;
    private final String status;
    private final String notes;
    private final List<OrderItemResponseDTO> orderItems;
    private final ChargeResponseDTO charges;

}