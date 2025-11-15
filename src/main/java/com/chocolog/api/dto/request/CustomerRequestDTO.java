package com.chocolog.api.dto.request;

import com.chocolog.api.validation.ValidPhone; 
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomerRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Phone cannot be blank")
    @ValidPhone
    private String phone;

    @Builder.Default
    private Boolean isReseller = false;

    private String notes;

}