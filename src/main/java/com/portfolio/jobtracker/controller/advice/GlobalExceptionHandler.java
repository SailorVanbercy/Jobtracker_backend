package com.portfolio.jobtracker.controller.advice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.portfolio.jobtracker.exception.RessourceNotFoundException;

@ControllerAdvice 
public class GlobalExceptionHandler {

    // Gestion de l'exception RessourceNotFoundException (404 Not Found)
    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleRessourceNotFoundException(RessourceNotFoundException ex){
        Map<String,String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException ex){
        Map<String,String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String,String>> handleBadCredentials(BadCredentialsException ex){
        Map<String,String> response = new HashMap<>();
        response.put("error", "Email ou mot de passe incorrect");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Gestion des doublons en base de données (ex: email déjà utilisé)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> handleDataIntegrityViolation(DataIntegrityViolationException ex){
        Map<String,String> response = new HashMap<>();
        response.put("error", "Cet email est déjà utilisé par un autre compte.");
        
        // 409 CONFLICT est le code HTTP standard pour un doublon
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
