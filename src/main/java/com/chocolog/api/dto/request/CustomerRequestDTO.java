package com.chocolog.api.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class CustomerRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String phone;

    @NotNull(message = "isReseller cannot be null")
    private Boolean isReseller;

    private String notes;
}