// Stage 2 — REST API error handling
package com.syncboard.api.error;

import com.syncboard.api.dto.ErrorResponse;
import com.syncboard.domain.DomainErrorCode;
import com.syncboard.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Produces the contract's exact {@link ErrorResponse} envelope for every 4xx/5xx
 * (01-CONTRACT.md §4). Never leaks a stack trace into the response body.
 *
 * <p>A1's domain layer exposes a common abstract {@code com.syncboard.domain.DomainException}
 * (accessor {@code code(): DomainErrorCode}, not a {@code getErrorCode()} bean-style getter) that
 * every concrete domain rule violation (illegal transition, missing assignee, missing reason,
 * ...) extends, so a single handler here can catch them polymorphically.
 *
 * <p>Because a single {@code NoSuchElementException} can't say which resource type was missing,
 * this class' own service-layer callers are required (by convention, within this same api+service
 * codebase) to start the exception message with the resource name — "Ticket not found: ...",
 * "User not found: ...", "Comment not found: ...", "Link not found: ..." — so
 * {@link #resolveNotFoundCode(String)} can pick the right 404 code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(ApiErrorCode.VALIDATION_FAILED, "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex, HttpServletRequest request) {
        return build(resolveDomainCode(ex.code()), ex.getMessage(), request, null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleVersionConflict(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return build(ApiErrorCode.VERSION_CONFLICT, "This record was modified by someone else; reload and try again", request, null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        return build(resolveNotFoundCode(ex.getMessage()), ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(ApiErrorCode.INTERNAL_ERROR, "An unexpected error occurred", request, null);
    }

    private ApiErrorCode resolveDomainCode(DomainErrorCode errorCode) {
        if (errorCode == null) {
            return ApiErrorCode.INTERNAL_ERROR;
        }
        try {
            return ApiErrorCode.valueOf(errorCode.name());
        } catch (IllegalArgumentException unmapped) {
            return ApiErrorCode.INTERNAL_ERROR;
        }
    }

    private ApiErrorCode resolveNotFoundCode(String message) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("user")) {
            return ApiErrorCode.USER_NOT_FOUND;
        }
        if (normalized.startsWith("comment")) {
            return ApiErrorCode.COMMENT_NOT_FOUND;
        }
        if (normalized.startsWith("link")) {
            return ApiErrorCode.LINK_NOT_FOUND;
        }
        return ApiErrorCode.TICKET_NOT_FOUND;
    }

    private ResponseEntity<ErrorResponse> build(ApiErrorCode code, String message, HttpServletRequest request, Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                code.httpStatus().value(),
                code.name(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(code.httpStatus()).body(body);
    }
}
