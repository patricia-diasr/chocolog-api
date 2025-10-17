package com.chocolog.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureOrPresentDateValidator.class)
@Documented
public @interface FutureOrPresentDate {

    String message() default "Date must be in the present or in the future.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}