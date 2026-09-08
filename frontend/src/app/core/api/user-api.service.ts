// Stage 4 — Angular 22 shell

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';

import type { UserSummary } from '@core/models';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  /** GET /api/v1/users?activeOnly= */
  getUsers(activeOnly?: boolean): Observable<UserSummary[]> {
    let params = new HttpParams();
    if (activeOnly != null) {
      params = params.set('activeOnly', String(activeOnly));
    }
    return this.http.get<UserSummary[]>(`${this.baseUrl}/users`, { params });
  }

  /** GET /api/v1/me */
  getMe(): Observable<UserSummary> {
    return this.http.get<UserSummary>(`${this.baseUrl}/me`);
  }
}
