package io.nuvalence.web.portal.service.controllers;

import io.nuvalence.web.portal.service.generated.models.Error;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Returns error response, if exception is thrown in the code.
 */
@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {
    /**
     * Enumeration of error codes.
     */
    @RequiredArgsConstructor
    public enum ErrorCode {
        GENERAL_ERROR("General-Error");

        @Getter private final String code;
    }

    /**
     * Handle any auth related exceptions.
     *
     * @param e Exception to handle
     * @return 403 response with empty body
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication error", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Handle any exception that is not handled by more precise handler.
     *
     * @param e Exception to handle
     * @return 500 response with generic error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleException(Exception e) {
        log.error("Uncaught exception thrown from controller.", e);
        return ResponseEntity.internalServerError()
                .body(
                        new Error()
                                .code(ErrorCode.GENERAL_ERROR.code)
                                .message("Unexpected Server Error"));
    }
}
