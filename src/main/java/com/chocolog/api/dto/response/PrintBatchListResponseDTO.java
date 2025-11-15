package com.chocolog.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PrintBatchListResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private EmployeeResponseDTO printedBy;
    private int itemsCount;

}