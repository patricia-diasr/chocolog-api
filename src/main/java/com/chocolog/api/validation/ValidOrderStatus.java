package com.chocolog.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderSatusValidator.class)
@Documented
public @interface ValidOrderStatus {
    String message() default "Invalid status. Must be one of: PENDING, READY_FOR_PICKUP, COMPLETED, CANCELLED";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}