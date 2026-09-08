// Stage 4 — Angular 22 shell

import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

const DEFAULT_DURATION_MS = 5000;

/** Thin MatSnackBar wrapper used by the error interceptor and feature code alike. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  error(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      duration: DEFAULT_DURATION_MS,
      panelClass: 'sb-snackbar-error',
    });
  }

  info(message: string): void {
    this.snackBar.open(message, undefined, {
      duration: DEFAULT_DURATION_MS,
      panelClass: 'sb-snackbar-info',
    });
  }
}
