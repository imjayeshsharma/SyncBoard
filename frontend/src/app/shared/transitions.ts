// Stage 4 — Angular 22 feature screens
// Legal ticket-status transition map — mirrors contract §2 "Legal status transitions"
// exactly (single source of truth is the backend; this is the UI's read-only mirror used
// to decide which options the status selector on the Ticket Detail screen offers).
//
// OPEN         -> TRIAGING, CANCELLED
// TRIAGING     -> IN_PROGRESS, CANCELLED
// IN_PROGRESS  -> UNDER_REVIEW, BLOCKED, CANCELLED
// UNDER_REVIEW -> COMPLETED, IN_PROGRESS, BLOCKED, CANCELLED
// BLOCKED      -> <the status it was blocked from>, CANCELLED
// COMPLETED    -> (terminal)
// CANCELLED    -> (terminal)

import type { BoardColumn, TicketStatus } from '@core/models';

/** Board-column-eligible statuses in contract §2 left-to-right order (Cancelled excluded). */
export const BOARD_COLUMN_ORDER: readonly TicketStatus[] = [
  'Open',
  'Triaging',
  'InProgress',
  'UnderReview',
  'Blocked',
  'Completed',
];

/** Human-readable label for each TicketStatus, per contract §2's "Board column label" column. */
export const STATUS_LABELS: Readonly<Record<TicketStatus, string>> = {
  Open: 'Open',
  Triaging: 'Triaging',
  InProgress: 'In Progress',
  UnderReview: 'Under Review',
  Blocked: 'Blocked',
  Completed: 'Completed',
  Cancelled: 'Cancelled',
};

const STATIC_TRANSITIONS: Readonly<Record<TicketStatus, readonly TicketStatus[]>> = {
  Open: ['Triaging', 'Cancelled'],
  Triaging: ['InProgress', 'Cancelled'],
  InProgress: ['UnderReview', 'Blocked', 'Cancelled'],
  UnderReview: ['Completed', 'InProgress', 'Blocked', 'Cancelled'],
  // Blocked is special-cased in allowedNext() below (depends on blockedFromStatus).
  Blocked: ['Cancelled'],
  Completed: [],
  Cancelled: [],
};

/**
 * Returns the set of statuses `current` may legally transition to next.
 * When `current` is `Blocked`, the only non-Cancelled option is `blockedFrom`
 * (the status the ticket was blocked from), per contract §2.
 */
export function allowedNext(
  current: TicketStatus,
  blockedFrom?: TicketStatus | null,
): TicketStatus[] {
  if (current === 'Blocked') {
    return blockedFrom ? [blockedFrom, 'Cancelled'] : ['Cancelled'];
  }
  return [...STATIC_TRANSITIONS[current]];
}

/** True if `-> COMPLETED` requires an assignee (contract §2 "ASSIGNEE_REQUIRED"). */
export function requiresAssignee(toStatus: TicketStatus): boolean {
  return toStatus === 'Completed';
}

/** True if `-> CANCELLED` / `-> BLOCKED` requires a non-blank note ("REASON_REQUIRED"). */
export function requiresNote(toStatus: TicketStatus): boolean {
  return toStatus === 'Cancelled' || toStatus === 'Blocked';
}

/**
 * Reindexes `columns` (as returned by BoardStore/GET /api/v1/board) against the fixed
 * contract §2 board order, defensively filling in an empty column for any status BoardStore
 * hasn't populated yet (e.g. before its first load), so board-page / my-tasks-page always
 * render all six columns — never fewer, never out of order.
 */
export function toOrderedColumns(columns: readonly BoardColumn[]): BoardColumn[] {
  const byStatus = new Map(columns.map((column) => [column.status, column]));
  return BOARD_COLUMN_ORDER.map(
    (status) => byStatus.get(status) ?? { status, label: STATUS_LABELS[status], tickets: [] },
  );
}
