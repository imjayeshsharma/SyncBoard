// Stage 4 — Angular 22 shell

import { Injectable, inject, signal } from '@angular/core';

import { TicketApiService } from '@core/api/ticket-api.service';
import type {
  CreateTicketRequest,
  TicketDetail,
  TransitionRequest,
  UpdateTicketRequest,
} from '@core/models';

/** Holds the ticket currently open in the detail/edit view. */
@Injectable({ providedIn: 'root' })
export class TicketStore {
  private readonly ticketApi = inject(TicketApiService);

  private readonly _ticket = signal<TicketDetail | null>(null);
  readonly ticket = this._ticket.asReadonly();

  private readonly _loading = signal(false);
  readonly loading = this._loading.asReadonly();

  private readonly _error = signal<string | null>(null);
  readonly error = this._error.asReadonly();

  /** GET /api/v1/tickets/{id} */
  loadTicket(id: string): void {
    this._loading.set(true);
    this._error.set(null);
    this.ticketApi.getTicket(id).subscribe({
      next: (ticket) => {
        this._ticket.set(ticket);
        this._loading.set(false);
      },
      error: () => {
        this._error.set('Unable to load ticket');
        this._loading.set(false);
      },
    });
  }

  clear(): void {
    this._ticket.set(null);
    this._error.set(null);
  }

  // TODO(stage 5): implement create/update/transition against TicketApiService,
  // following the same optimistic-update-with-rollback pattern as BoardStore.moveTicket.

  createTicket(_request: CreateTicketRequest): void {
    throw new Error('TODO(stage 5): TicketStore.createTicket');
  }

  updateTicket(_id: string, _request: UpdateTicketRequest): void {
    throw new Error('TODO(stage 5): TicketStore.updateTicket');
  }

  transition(_id: string, _request: TransitionRequest): void {
    throw new Error('TODO(stage 5): TicketStore.transition');
  }
}
