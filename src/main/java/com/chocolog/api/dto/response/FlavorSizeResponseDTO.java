package com.chocolog.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlavorSizeResponseDTO {

    private Long sizeId;
    private String name;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private Integer totalQuantity;
    private Integer remainingQuantity;

}