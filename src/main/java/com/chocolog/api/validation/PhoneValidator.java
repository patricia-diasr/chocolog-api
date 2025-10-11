package com.chocolog.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");

        int length = digitsOnly.length();
        return length == 10 || length == 11;
    }
}