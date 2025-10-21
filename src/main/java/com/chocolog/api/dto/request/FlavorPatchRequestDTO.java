package com.chocolog.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class FlavorPatchRequestDTO {

    private String name;

    @JsonProperty("sizes")
    private List<PriceRequestDTO> prices;

}