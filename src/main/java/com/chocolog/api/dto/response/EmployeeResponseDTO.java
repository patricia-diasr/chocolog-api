package com.chocolog.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeResponseDTO {

    private final Long id;
    private final String name;
    private final String login;
    private final String role;
    
}