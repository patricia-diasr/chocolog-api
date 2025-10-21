package com.chocolog.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class FutureOrPresentDateValidator implements ConstraintValidator<FutureOrPresentDate, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime dateToValidate, ConstraintValidatorContext context) {
        if (dateToValidate == null) {
            return true;
        }

        return !dateToValidate.isBefore(LocalDateTime.now());
    }
}