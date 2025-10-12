package com.chocolog.api.dto.request;

import com.chocolog.api.validation.ValidPhone; 
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlavorRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

}