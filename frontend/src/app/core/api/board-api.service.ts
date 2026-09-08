// Stage 4 — Angular 22 shell

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';

import type { BoardResponse } from '@core/models';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class BoardApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  /** GET /api/v1/board */
  getBoard(): Observable<BoardResponse> {
    return this.http.get<BoardResponse>(`${this.baseUrl}/board`);
  }
}
