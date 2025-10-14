package com.chocolog.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime; 
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockRecordResponseDTO {

    private Long id;
    private String flavorName;
    private String sizeName;
    private Integer quantity;
    private LocalDateTime productionDate;
    private LocalDateTime expirationDate;
    private String movementType;

}