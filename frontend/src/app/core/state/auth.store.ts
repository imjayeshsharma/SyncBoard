// Stage 4 — Angular 22 shell

import { Injectable, computed, inject, signal } from '@angular/core';

import { UserApiService } from '@core/api/user-api.service';
import type { UserSummary } from '@core/models';

/** Holds the signed-in user (via Google SSO — session cookie set by the backend). */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly userApi = inject(UserApiService);

  private readonly _currentUser = signal<UserSummary | null>(null);
  readonly currentUser = this._currentUser.asReadonly();

  private readonly _loading = signal(false);
  readonly loading = this._loading.asReadonly();

  private readonly _error = signal<string | null>(null);
  readonly error = this._error.asReadonly();

  readonly isAuthenticated = computed(() => this._currentUser() !== null);
  readonly initials = computed(() => initialsOf(this._currentUser()?.fullName));

  /** GET /api/v1/me — populates currentUser, e.g. from an app-init resolver. */
  loadCurrentUser(): void {
    this._loading.set(true);
    this._error.set(null);
    this.userApi.getMe().subscribe({
      next: (user) => {
        this._currentUser.set(user);
        this._loading.set(false);
      },
      error: () => {
        this._currentUser.set(null);
        this._error.set('Unable to load current user');
        this._loading.set(false);
      },
    });
  }
}

function initialsOf(fullName: string | undefined): string {
  if (!fullName) {
    return '';
  }
  return fullName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('');
}
