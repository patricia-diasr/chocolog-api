package com.chocolog.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoleValidator.class)
@Documented
public @interface ValidRole {
    String message() default "Invalid role. Must be one of: STAFF, ADMIN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}