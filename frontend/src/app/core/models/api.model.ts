// Stage 4 — Angular 22 shell
// Mirrors contract §4: PageResponse<T>, and the §4 error envelope as ApiError.

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type ApiErrorCode =
  | 'VALIDATION_FAILED'
  | 'ILLEGAL_TRANSITION'
  | 'ASSIGNEE_REQUIRED'
  | 'REASON_REQUIRED'
  | 'VERSION_CONFLICT'
  | 'TICKET_NOT_FOUND'
  | 'USER_NOT_FOUND'
  | 'COMMENT_NOT_FOUND'
  | 'LINK_NOT_FOUND'
  | 'UNAUTHENTICATED'
  | 'FORBIDDEN_DOMAIN'
  | 'INTERNAL_ERROR';

export interface ApiError {
  timestamp: string;
  status: number;
  code: ApiErrorCode;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}
