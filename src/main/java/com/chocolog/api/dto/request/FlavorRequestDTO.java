package com.chocolog.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class FlavorRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @JsonProperty("sizes")
    private List<PriceRequestDTO> prices;

}