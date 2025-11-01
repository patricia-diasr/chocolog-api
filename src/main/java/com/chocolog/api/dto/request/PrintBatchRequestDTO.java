package com.chocolog.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrintBatchRequestDTO {

    @NotEmpty(message = "Order item IDs cannot be empty")
    private List<Long> orderItemIds;

}