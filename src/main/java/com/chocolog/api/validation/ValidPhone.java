package com.chocolog.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD}) 
@Retention(RetentionPolicy.RUNTIME) 
@Constraint(validatedBy = PhoneValidator.class)
public @interface ValidPhone {
    
    String message() default "Invalid phone number. The number must have 10 or 11 digits.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}