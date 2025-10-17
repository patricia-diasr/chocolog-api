package com.chocolog.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FutureOrPresentDateValidator implements ConstraintValidator<FutureOrPresentDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate dateToValidate, ConstraintValidatorContext context) {
        if (dateToValidate == null) {
            return true;
        }

        return !dateToValidate.isBefore(LocalDate.now());
    }
}