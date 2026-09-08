// Stage 4 — Angular 22 shell
// Mirrors contract §4: BoardResponse, BoardColumn. Also declares the STOMP
// /topic/board event payload shape from contract §4 "Realtime".

import type { TicketSummary, TicketStatus } from './ticket.model';

export interface BoardColumn {
  status: TicketStatus;
  label: string;
  tickets: TicketSummary[];
}

export interface BoardResponse {
  columns: BoardColumn[];
  generatedAt: string;
}

export type BoardEventType = 'TICKET_MOVED' | 'TICKET_CREATED' | 'TICKET_UPDATED';

/** Payload of messages on STOMP topic `/topic/board` — contract §4 Realtime. */
export interface BoardEvent {
  type: BoardEventType;
  ticketId: string;
  fromStatus?: TicketStatus;
  toStatus?: TicketStatus;
  at: string;
}
