package com.chocolog.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrintBatchRequestDTO {

    @NotNull(message = "Employee ID cannot be null")
    private Long employeeId;

    @NotEmpty(message = "Order item IDs cannot be empty")
    private List<Long> orderItemIds;
}