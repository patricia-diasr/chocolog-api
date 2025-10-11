package com.chocolog.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerPatchRequestDTO {

    private String name;

    private String phone;

    private Boolean isReseller;
    
    private String notes;
}