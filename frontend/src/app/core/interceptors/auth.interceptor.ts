// Stage 4 — Angular 22 shell

import type { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs';

/**
 * Attaches the session cookie to every request and, on a 401 from the API,
 * sends the browser to the backend's OAuth entry point (Google SSO —
 * see PRD auth flow / contract §4 UNAUTHENTICATED).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authedReq = req.clone({ withCredentials: true });

  return next(authedReq).pipe(
    tap({
      error: (err: unknown) => {
        if (isUnauthenticated(err)) {
          window.location.href = '/oauth2/authorization/google';
        }
      },
    }),
  );
};

function isUnauthenticated(err: unknown): boolean {
  return typeof err === 'object' && err !== null && 'status' in err && (err as { status: unknown }).status === 401;
}
