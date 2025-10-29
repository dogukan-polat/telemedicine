package com.dogukanpolat.telemedicine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateUserException(DuplicateUserException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "400");
        error.put("error", "Duplicate User");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Map<String, String>> handlePasswordMismatchException(PasswordMismatchException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "400");
        error.put("error", "Password Mismatch");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "401");
        error.put("error", "Bad Credentials");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "400");
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AvailabilityException.class)
    public ResponseEntity<Map<String, String>> handleAvailabilityException(AvailabilityException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "400");
        error.put("error", "Availability Exception");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "400");
        error.put("error", "Illegal Argument Exception");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "500");
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.internalServerError().body(error);
    }
}
