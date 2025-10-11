package com.chocolog.api.controller.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity error. The provided value may already exist.";
        if (ex.getCause() != null && ex.getCause().getMessage().contains("ConstraintViolationException")) {
             message = "The provided value violates a uniqueness constraint (e.g., login or email is already in use).";
        }
        return new ResponseEntity<>(Map.of("error", message), HttpStatus.CONFLICT);
    }
}