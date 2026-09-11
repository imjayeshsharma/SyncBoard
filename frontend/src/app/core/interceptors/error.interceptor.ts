// Stage 4 — Angular 22 shell

import { HttpErrorResponse, type HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import type { ApiError } from '@core/models';
import { NotificationService } from '@core/notifications/notification.service';

/**
 * Parses the contract §4 error envelope out of failed responses, surfaces it
 * via a snackbar, and re-throws the parsed ApiError so callers can still
 * react to specific codes (e.g. VERSION_CONFLICT).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);

  return next(req).pipe(
    catchError((err: unknown) => {
      const apiError = toApiError(err, req.urlWithParams);
      notifications.error(apiError.message);
      return throwError(() => apiError);
    }),
  );
};

function toApiError(err: unknown, path: string): ApiError {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as Partial<ApiError> | null | undefined;
    if (body && typeof body === 'object' && 'code' in body) {
      return {
        timestamp: body.timestamp ?? new Date().toISOString(),
        status: body.status ?? err.status,
        code: body.code ?? 'INTERNAL_ERROR',
        message: body.message ?? err.message,
        path: body.path ?? path,
        fieldErrors: body.fieldErrors,
      };
    }
    return {
      timestamp: new Date().toISOString(),
      status: err.status,
      code: 'INTERNAL_ERROR',
      message: err.message || 'Unexpected error',
      path,
    };
  }
  return {
    timestamp: new Date().toISOString(),
    status: 0,
    code: 'INTERNAL_ERROR',
    message: 'Unexpected error',
    path,
  };
}
