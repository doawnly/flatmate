package com.flatmate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
}