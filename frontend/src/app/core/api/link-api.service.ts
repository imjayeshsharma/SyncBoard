// Stage 4 — Angular 22 shell

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';

import type { CreateLinkRequest, TicketLink } from '@core/models';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class LinkApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  /** GET /api/v1/tickets/{id}/links */
  getLinks(ticketId: string): Observable<TicketLink[]> {
    return this.http.get<TicketLink[]>(`${this.baseUrl}/tickets/${ticketId}/links`);
  }

  /** POST /api/v1/tickets/{id}/links */
  createLink(ticketId: string, request: CreateLinkRequest): Observable<TicketLink> {
    return this.http.post<TicketLink>(`${this.baseUrl}/tickets/${ticketId}/links`, request);
  }

  /** DELETE /api/v1/tickets/{id}/links/{linkId} */
  deleteLink(ticketId: string, linkId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tickets/${ticketId}/links/${linkId}`);
  }
}
