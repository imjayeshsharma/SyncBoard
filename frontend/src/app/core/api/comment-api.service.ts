// Stage 4 — Angular 22 shell

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import type { Observable } from 'rxjs';

import type { CommentNode, CreateCommentRequest } from '@core/models';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class CommentApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  /** GET /api/v1/tickets/{id}/comments */
  getComments(ticketId: string): Observable<CommentNode[]> {
    return this.http.get<CommentNode[]>(`${this.baseUrl}/tickets/${ticketId}/comments`);
  }

  /** POST /api/v1/tickets/{id}/comments */
  createComment(ticketId: string, request: CreateCommentRequest): Observable<CommentNode> {
    return this.http.post<CommentNode>(`${this.baseUrl}/tickets/${ticketId}/comments`, request);
  }
}
