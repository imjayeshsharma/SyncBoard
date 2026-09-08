// Stage 4 — Angular 22 shell
// Mirrors contract §2 (wire values) and §4: TicketSummary, TicketDetail,
// CreateTicketRequest, UpdateTicketRequest, TransitionRequest.

import type { UserSummary } from './user.model';
import type { TicketLink, CreateLinkRequest } from './link.model';
import type { CommentNode } from './comment.model';
import type { StatusHistoryEntry } from './history.model';

/** Wire values for `com.syncboard.domain.TicketStatus` — contract §2. */
export type TicketStatus =
  | 'Open'
  | 'Triaging'
  | 'InProgress'
  | 'Blocked'
  | 'UnderReview'
  | 'Completed'
  | 'Cancelled';

/** Wire values for `com.syncboard.domain.Priority` — contract §2. */
export type Priority = 'Critical' | 'High' | 'Medium' | 'Low';

export interface TicketStatusOption {
  status: TicketStatus;
  label: string;
}

/** All statuses, contract order. `Cancelled` is filter-only, not a board column. */
export const TICKET_STATUSES: readonly TicketStatusOption[] = [
  { status: 'Open', label: 'Open' },
  { status: 'Triaging', label: 'Triaging' },
  { status: 'InProgress', label: 'In Progress' },
  { status: 'UnderReview', label: 'Under Review' },
  { status: 'Blocked', label: 'Blocked' },
  { status: 'Completed', label: 'Completed' },
  { status: 'Cancelled', label: 'Cancelled' },
];

/** Board column order left→right — contract §2: Open, Triaging, InProgress, UnderReview, Blocked, Completed. */
export const BOARD_COLUMNS: readonly TicketStatusOption[] = [
  { status: 'Open', label: 'Open' },
  { status: 'Triaging', label: 'Triaging' },
  { status: 'InProgress', label: 'In Progress' },
  { status: 'UnderReview', label: 'Under Review' },
  { status: 'Blocked', label: 'Blocked' },
  { status: 'Completed', label: 'Completed' },
];

export const PRIORITIES: readonly Priority[] = ['Critical', 'High', 'Medium', 'Low'];

export interface TicketSummary {
  id: string;
  title: string;
  status: TicketStatus;
  priority: Priority;
  category?: string;
  assignee?: UserSummary;
  reporter: UserSummary;
  dueAt?: string;
  overdue: boolean;
  commentCount: number;
  linkCount: number;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface TicketDetail extends TicketSummary {
  description?: string;
  blockedFromStatus?: TicketStatus;
  closedAt?: string;
  links: TicketLink[];
  comments: CommentNode[];
  history: StatusHistoryEntry[];
}

export interface CreateTicketRequest {
  title: string;
  description?: string;
  category?: string;
  priority: Priority;
  assigneeId?: string;
  dueAt?: string;
  links?: CreateLinkRequest[];
}

export interface UpdateTicketRequest {
  title?: string;
  description?: string;
  category?: string;
  priority?: Priority;
  assigneeId?: string;
  dueAt?: string;
  version: number;
}

export interface TransitionRequest {
  toStatus: TicketStatus;
  note?: string;
  version: number;
}

export interface TicketQuery {
  status?: TicketStatus;
  assigneeId?: string;
  priority?: Priority;
  q?: string;
  page?: number;
  size?: number;
}
