package com.chocolog.api.dto.request;

import com.chocolog.api.validation.ValidOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderPatchRequestDTO {

    @ValidOrderStatus
    private String status;

    private LocalDateTime expectedPickupDate;
    private String notes;
    private BigDecimal discount;

}