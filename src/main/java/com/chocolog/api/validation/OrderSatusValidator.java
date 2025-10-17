package com.chocolog.api.validation;

import com.chocolog.api.model.OrderStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class OrderSatusValidator implements ConstraintValidator<ValidOrderStatus, String> {

    @Override
    public boolean isValid(String statusName, ConstraintValidatorContext context) {
        if (statusName == null) {
            return true;
        }

        return Arrays.stream(OrderStatus.values())
                .anyMatch(status -> status.name().equalsIgnoreCase(statusName));
    }
}