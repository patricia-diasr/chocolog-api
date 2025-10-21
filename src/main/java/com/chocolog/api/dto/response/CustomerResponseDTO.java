package com.chocolog.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerResponseDTO {

    private final Long id;
    private final String name;
    private final String phone;
    private final Boolean isReseller;
    private final String notes;

}