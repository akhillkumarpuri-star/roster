package com.example.roster.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("message", "Correct the highlighted fields.", "errors", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> unreadable() {
        return ResponseEntity.badRequest().body(Map.of("message", "The request body is malformed."));
    }

    @ExceptionHandler(ApiExceptions.BadRequest.class)
    ResponseEntity<Map<String, Object>> badRequest(ApiExceptions.BadRequest ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage(),
                "errors", Map.of(ex.getField(), ex.getMessage())));
    }

    @ExceptionHandler(ApiExceptions.NotFound.class)
    ResponseEntity<Map<String, Object>> notFound(ApiExceptions.NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ApiExceptions.Conflict.class)
    ResponseEntity<Map<String, Object>> conflict(ApiExceptions.Conflict ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage(),
                "errors", Map.of("email", ex.getMessage())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> integrity() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "That value already exists."));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, Object>> badCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password."));
    }
}
