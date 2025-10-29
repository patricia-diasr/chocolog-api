package com.chocolog.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrintBatchResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private Long printedByEmployeeId;
    private String fileSystemPath;
    private List<OrderItemResponseDTO> items;

}