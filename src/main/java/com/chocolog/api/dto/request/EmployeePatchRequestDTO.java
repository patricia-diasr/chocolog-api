package com.chocolog.api.dto.request;

import com.chocolog.api.validation.ValidRole; 
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeePatchRequestDTO {

    private String name;
    private String login;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @ValidRole
    private String role;
    
}