package com.chocolog.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PrintBatchListResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private EmployeeResponseDTO printedBy;
    private int itemsCount;

}