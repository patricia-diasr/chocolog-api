package com.chocolog.api.dto.request;

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
public class OrderRequestDTO {

    @NotNull(message = "Expected pickup date cannot be null")
    private LocalDateTime expectedPickupDate;

    private String notes;
    private BigDecimal discount;

    @NotEmpty(message = "Order must have at least one item")
    private List<@Valid OrderItemRequestDTO> orderItems;

}