package com.chocolog.api.dto.request;

import com.chocolog.api.validation.ValidOrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDTO {

    @NotNull(message = "Size ID cannot be null")
    private Long sizeId;

    @NotNull(message = "Flavor1 ID cannot be null")
    private Long flavor1Id;

    private Long flavor2Id;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String notes;

    @ValidOrderStatus
    private String status;
}