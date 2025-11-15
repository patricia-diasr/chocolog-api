package com.chocolog.api.dto.response;

import lombok.*;

import java.time.LocalDateTime; 
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRecordResponseDTO {

    private Long id;
    private String flavorName;
    private String sizeName;
    private Integer quantity;
    private LocalDateTime productionDate;
    private LocalDateTime expirationDate;
    private String movementType;

}