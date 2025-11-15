package com.chocolog.api.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@Builder
public class StockRecordRequestDTO {

    @NotNull(message = "Flavor ID cannot be null")
    private Long flavorId;

    @NotNull(message = "Size ID cannot be null")
    private Long sizeId;

    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    @NotBlank(message = "Movement type cannot be blank")
    @Pattern(regexp = "INBOUND|OUTBOUND", message = "Movement type must be INBOUND or OUTBOUND")
    private String movementType;
}