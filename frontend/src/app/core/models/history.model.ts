// Stage 4 — Angular 22 shell
// Mirrors contract §4: StatusHistoryEntry

import type { UserSummary } from './user.model';
import type { TicketStatus } from './ticket.model';

export interface StatusHistoryEntry {
  id: string;
  fromStatus?: TicketStatus;
  toStatus: TicketStatus;
  changedBy: UserSummary;
  changedAt: string;
  note?: string;
}
