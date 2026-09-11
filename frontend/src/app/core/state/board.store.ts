// Stage 4 — Angular 22 shell

import { Injectable, computed, inject, signal } from '@angular/core';

import { BoardApiService } from '@core/api/board-api.service';
import { TicketApiService } from '@core/api/ticket-api.service';
import type { BoardColumn, BoardResponse, TicketStatus, TicketSummary } from '@core/models';
import { NotificationService } from '@core/notifications/notification.service';
import { AuthStore } from '@core/state/auth.store';

/** Holds the board (contract §4 `GET /api/v1/board`) and drives drag-and-drop moves. */
@Injectable({ providedIn: 'root' })
export class BoardStore {
  private readonly boardApi = inject(BoardApiService);
  private readonly ticketApi = inject(TicketApiService);
  private readonly notifications = inject(NotificationService);
  private readonly authStore = inject(AuthStore);

  private readonly _board = signal<BoardResponse | null>(null);
  readonly board = this._board.asReadonly();

  private readonly _loading = signal(false);
  readonly loading = this._loading.asReadonly();

  private readonly _error = signal<string | null>(null);
  readonly error = this._error.asReadonly();

  readonly columns = computed<BoardColumn[]>(() => this._board()?.columns ?? []);

  readonly allTickets = computed<TicketSummary[]>(() =>
    this.columns().flatMap((column) => column.tickets),
  );

  readonly overdueCount = computed(() => this.allTickets().filter((t) => t.overdue).length);

  readonly myTickets = computed<TicketSummary[]>(() => {
    const me = this.authStore.currentUser();
    if (!me) {
      return [];
    }
    return this.allTickets().filter((t) => t.assignee?.id === me.id);
  });

  /** GET /api/v1/board */
  loadBoard(): void {
    this._loading.set(true);
    this._error.set(null);
    this.boardApi.getBoard().subscribe({
      next: (board) => {
        this._board.set(board);
        this._loading.set(false);
      },
      error: () => {
        this._error.set('Unable to load board');
        this._loading.set(false);
      },
    });
  }

  /**
   * Optimistic move: mutates the board signal immediately so drag-and-drop
   * feels instant, fires `POST /tickets/{id}/transitions`, and rolls back to
   * the pre-move snapshot (plus a snackbar) if the server rejects it —
   * PRD "Concurrent Edits" / optimistic update with rollback.
   */
  moveTicket(ticketId: string, toStatus: TicketStatus, note?: string): void {
    const previous = this._board();
    if (!previous) {
      return;
    }

    const ticket = previous.columns
      .flatMap((column) => column.tickets)
      .find((t) => t.id === ticketId);
    if (!ticket) {
      return;
    }

    this._board.set(applyOptimisticMove(previous, ticketId, toStatus));

    this.ticketApi.transition(ticketId, { toStatus, note, version: ticket.version }).subscribe({
      next: (updated) => {
        const current = this._board();
        if (current) {
          this._board.set(patchTicket(current, updated));
        }
      },
      error: (err: unknown) => {
        this._board.set(previous);
        const message = isApiErrorMessage(err) ? err.message : 'Could not move ticket — reverted.';
        this.notifications.error(message);
      },
    });
  }
}

function applyOptimisticMove(
  board: BoardResponse,
  ticketId: string,
  toStatus: TicketStatus,
): BoardResponse {
  let moved: TicketSummary | undefined;

  const withoutTicket = board.columns.map((column) => {
    const found = column.tickets.find((t) => t.id === ticketId);
    if (found) {
      moved = { ...found, status: toStatus };
    }
    return { ...column, tickets: column.tickets.filter((t) => t.id !== ticketId) };
  });

  if (!moved) {
    return board;
  }

  const targetExists = withoutTicket.some((column) => column.status === toStatus);
  const columns = withoutTicket.map((column) =>
    column.status === toStatus ? { ...column, tickets: [moved as TicketSummary, ...column.tickets] } : column,
  );

  // Target status has no board column (e.g. Cancelled) — ticket simply leaves the board.
  return { ...board, columns: targetExists ? columns : withoutTicket };
}

function patchTicket(board: BoardResponse, updated: TicketSummary): BoardResponse {
  return {
    ...board,
    columns: board.columns.map((column) => ({
      ...column,
      tickets: column.tickets.map((t) => (t.id === updated.id ? { ...t, ...updated } : t)),
    })),
  };
}

function isApiErrorMessage(err: unknown): err is { message: string } {
  return typeof err === 'object' && err !== null && 'message' in err && typeof (err as { message: unknown }).message === 'string';
}
