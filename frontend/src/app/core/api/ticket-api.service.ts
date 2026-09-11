// Stage 4 — Angular 22 shell

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';

import type {
  CreateTicketRequest,
  PageResponse,
  StatusHistoryEntry,
  TicketDetail,
  TicketQuery,
  TicketSummary,
  TransitionRequest,
  UpdateTicketRequest,
} from '@core/models';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class TicketApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  /** GET /api/v1/tickets */
  getTickets(query: TicketQuery = {}): Observable<PageResponse<TicketSummary>> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null) {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<PageResponse<TicketSummary>>(`${this.baseUrl}/tickets`, { params });
  }

  /** POST /api/v1/tickets */
  createTicket(request: CreateTicketRequest): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(`${this.baseUrl}/tickets`, request);
  }

  /** GET /api/v1/tickets/{id} */
  getTicket(id: string): Observable<TicketDetail> {
    return this.http.get<TicketDetail>(`${this.baseUrl}/tickets/${id}`);
  }

  /** PATCH /api/v1/tickets/{id} */
  updateTicket(id: string, request: UpdateTicketRequest): Observable<TicketDetail> {
    return this.http.patch<TicketDetail>(`${this.baseUrl}/tickets/${id}`, request);
  }

  /** POST /api/v1/tickets/{id}/transitions */
  transition(id: string, request: TransitionRequest): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(`${this.baseUrl}/tickets/${id}/transitions`, request);
  }

  /** GET /api/v1/tickets/{id}/history */
  getHistory(id: string): Observable<StatusHistoryEntry[]> {
    return this.http.get<StatusHistoryEntry[]>(`${this.baseUrl}/tickets/${id}/history`);
  }
}
