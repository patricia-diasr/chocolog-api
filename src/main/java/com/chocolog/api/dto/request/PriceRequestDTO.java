package com.chocolog.api.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class PriceRequestDTO {

    private Long sizeId;
    private BigDecimal salePrice;
    private BigDecimal costPrice;

}