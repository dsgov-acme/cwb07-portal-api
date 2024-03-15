package io.nuvalence.web.portal.service.controllers;

import static org.junit.jupiter.api.Assertions.*;

import io.nuvalence.web.portal.service.generated.models.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Objects;

class GlobalErrorHandlerTest {
    private GlobalErrorHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new GlobalErrorHandler();
    }

    @Test
    void handleAuthenticationException() {
        final ResponseEntity<Void> response =
                handler.handleAuthenticationException(new BadCredentialsException("oops"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.hasBody());
    }

    @Test
    void handleException() {
        final ResponseEntity<Error> response = handler.handleException(new Exception("oops"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(
                GlobalErrorHandler.ErrorCode.GENERAL_ERROR.getCode(),
                Objects.requireNonNull(response.getBody()).getCode());
    }
}
