// Stage 4 — Angular 22 shell
// Mirrors contract §4: CommentNode, CreateCommentRequest

import type { UserSummary } from './user.model';

export interface CommentNode {
  id: string;
  bodyText: string;
  author: UserSummary;
  createdAt: string;
  editedAt?: string;
  replies: CommentNode[];
}

export interface CreateCommentRequest {
  bodyText: string;
  parentCommentId?: string;
}
