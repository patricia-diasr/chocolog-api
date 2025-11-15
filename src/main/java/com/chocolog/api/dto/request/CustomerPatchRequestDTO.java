package com.chocolog.api.dto.request;

import com.chocolog.api.validation.ValidPhone;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomerPatchRequestDTO {

    private String name;
    
    @ValidPhone 
    private String phone;
    
    private Boolean isReseller;
    private String notes;

}