package com.burnafter.message_service.error;

import com.burnafter.message_service.dtos.ApiError;
import com.burnafter.message_service.exception.InvalidPasteException;
import com.burnafter.message_service.exception.PasteNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidPasteException.class)
    public ResponseEntity<ApiError> handleInvalidPaste(
            InvalidPasteException ex,
            HttpServletRequest req) {

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        "INVALID_PASTE",
                        ex.getReason().name(),
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(PasteNotFoundException.class)
    public ResponseEntity<ApiError> handlePasteNotFound(
            HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        "PASTE_NOT_FOUND",
                        "Paste not found",
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        "VALIDATION_ERROR",
                        "Invalid request",
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest req) {

        log.error("Unhandled exception on {}", req.getRequestURI(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "INTERNAL_ERROR",
                        "Internal server error",
                        req.getRequestURI(),
                        Instant.now()
                ));
    }
}

