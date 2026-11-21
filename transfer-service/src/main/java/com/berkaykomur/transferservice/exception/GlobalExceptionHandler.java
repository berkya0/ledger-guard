package com.berkaykomur.transferservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError> handleBaseException(BaseException e, HttpServletRequest request) {
        HttpStatus status = resolveHttpStatus(e);
        log.warn("Business rule violation [{}]: {} | Path: {}", e.getErrorCode(), e.getMessage(), request.getRequestURI());

        ApiError apiError = ApiError.builder()
                .error(e.getErrorCode())
                .status(status.value())
                .path(request.getRequestURI())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            validationErrors.merge(
                    error.getField(),
                    error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                    (existing, replacement) -> existing + "; " + replacement
            );
        }
        log.warn("Validation failed | Path: {}", request.getRequestURI());

        ApiError apiError = ApiError.builder()
                .error("VALIDATION_FAILED")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .message("Validation errors occurred.")
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception e, HttpServletRequest request) {
        log.error("Unexpected system error | Path: {}", request.getRequestURI(), e);

        ApiError apiError = ApiError.builder()
                .error("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .message("An unexpected error occurred.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private HttpStatus resolveHttpStatus(BaseException e) {

        if (e instanceof TransferNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (e instanceof SameAccountTransferException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}