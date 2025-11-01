package com.chocolog.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrintBatchDetailResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private String fileSystemPath;
    private EmployeeResponseDTO printedBy;
    private List<PrintBatchItemResponseDTO> items;

}