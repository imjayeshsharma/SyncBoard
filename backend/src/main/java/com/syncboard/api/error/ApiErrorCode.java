// Stage 2 — REST API error handling
package com.syncboard.api.error;

import org.springframework.http.HttpStatus;

/**
 * Every error code from contract 01-CONTRACT.md §4, paired with the HTTP status it maps to.
 * {@link GlobalExceptionHandler} is the only place that turns an exception into one of these.
 */
public enum ApiErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    ILLEGAL_TRANSITION(HttpStatus.UNPROCESSABLE_ENTITY),
    ASSIGNEE_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY),
    REASON_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY),
    VERSION_CONFLICT(HttpStatus.CONFLICT),
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    LINK_NOT_FOUND(HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN_DOMAIN(HttpStatus.FORBIDDEN),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ApiErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
