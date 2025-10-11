package com.chocolog.api.validation;

import com.chocolog.api.model.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {

    @Override
    public boolean isValid(String roleName, ConstraintValidatorContext context) {
        if (roleName == null) {
            return true; 
        }

        return Arrays.stream(Role.values())
                .anyMatch(role -> role.name().equalsIgnoreCase(roleName));
    }
}