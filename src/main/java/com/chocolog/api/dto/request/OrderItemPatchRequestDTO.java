package com.chocolog.api.dto.request;

import com.chocolog.api.validation.ValidOrderStatus;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemPatchRequestDTO {

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String notes;
    private Long sizeId;
    private Long flavor1Id;
    private Long flavor2Id;

    @ValidOrderStatus
    private String status;

}