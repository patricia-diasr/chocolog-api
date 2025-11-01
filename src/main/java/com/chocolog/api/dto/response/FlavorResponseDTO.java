package com.chocolog.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FlavorResponseDTO {

    private Long id;
    private String name;
    private List<FlavorSizeResponseDTO> sizes;

}