package com.chocolog.api.dto.response;

import com.chocolog.api.model.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {

    private String token;
    private Role role;

}
