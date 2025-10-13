package com.chocolog.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class FlavorPatchRequestDTO {

    private String name;
    private List<PriceRequestDTO> prices;
    
}