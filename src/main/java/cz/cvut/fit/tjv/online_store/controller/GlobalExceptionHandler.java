package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleIllegalStateException(IllegalStateException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());
        return response;
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid date format.");
            return ResponseEntity.badRequest().body(response);
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Malformed JSON or request body.");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException exception) {
        Map<String, String> response = new HashMap<>();
        if (exception.getMessage().contains("not found")) {
            response.put("error", "Not Found");
            response.put("message", exception.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("error", "Bad Request");
        response.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }



    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflictException(ConflictException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());
        return response;
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Forbidden");
        response.put("message", exception.getMessage());
        return response;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception exception) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> response = new HashMap<>();

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        for (FieldError error : fieldErrors) {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            if (!response.containsKey(fieldName)) {
                response.put("message", errorMessage);
            }
        }

        if (response.isEmpty()) {
            response.put("message", "Validation failed. Please check your input.");
        }
        System.out.println(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


}