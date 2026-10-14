package com.flatmate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldReturnBadRequestForIllegalArgumentException() {
        IllegalArgumentException exception =
                new IllegalArgumentException(
                        "Expense amount must be greater than zero"
                );

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals(
                "Expense amount must be greater than zero",
                response.getBody().message()
        );
    }

    @Test
    void shouldReturnNotFoundForResourceNotFoundException() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException("Payer not found: 999");

        ResponseEntity<ApiError> response =
                handler.handleResourceNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals(
                "Payer not found: 999",
                response.getBody().message()
        );
    }

    @Test
    void shouldReturnBadRequestForValidationErrors() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                "", BigDecimal.TEN, 1, 1,
                List.of(new ExpenseSplitRequest(1, BigDecimal.TEN))
        );
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(request, "request");
        bindingResult.addError(
                new FieldError("request", "description", "must not be blank")
        );
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response =
                handler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("description: must not be blank", response.getBody().message());
    }
    @Test
    void shouldReturnConflictForResponseStatusException() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email is already registered"
        );

        ResponseEntity<ApiError> response =
                handler.handleResponseStatusException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals(
                "Email is already registered",
                response.getBody().message()
        );
    }
    @Test
    void shouldReturnConflictForDataIntegrityViolationException() {
        org.springframework.dao.DataIntegrityViolationException exception =
                new org.springframework.dao.DataIntegrityViolationException("duplicate value");

        ResponseEntity<ApiError> response =
                handler.handleDataIntegrityViolationException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals(
                "A database constraint was violated",
                response.getBody().message()
        );
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() {
        RuntimeException exception =
                new RuntimeException("Sensitive internal details");

        ResponseEntity<ApiError> response =
                handler.handleUnexpectedException(exception);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals(
                "An unexpected error occurred",
                response.getBody().message()
        );
    }
}