package com.chocolog.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintBatchItemResponseDTO {

    private Long id;
    private OrderItemResponseDTO orderItem;

}