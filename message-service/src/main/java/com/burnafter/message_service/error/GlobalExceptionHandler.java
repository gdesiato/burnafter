package com.burnafter.message_service.error;

import com.burnafter.message_service.dtos.ApiError;
import com.burnafter.message_service.exception.InvalidPasteException;
import com.burnafter.message_service.exception.PasteNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPasteException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidPaste(InvalidPasteException ex) {
        return new ApiError(ex.getReason().name());
    }

    @ExceptionHandler(PasteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlePasteNotFound() {
        return new ApiError("Paste not found");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex) {
        return new ApiError("Internal server error");
    }
}

