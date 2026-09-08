// Stage 4 — Angular 22 feature screens
// PRD "Ticket Detail & Resource Hub" — route target for '/tickets/:id'. Two-column layout:
// left = title/description/comments/history, right = status/priority/assignee/due-date +
// linked resources.
//
// `id` is bound automatically from the route param by withComponentInputBinding() (A5's
// app.config.ts). TicketStore/TicketApiService/CommentApiService/LinkApiService are
// injected per the contract so the DI wiring is real, but this component renders from a
// view-local `ticket` signal seeded with mock data — the actual load/mutate calls against
// those services are left as TODO(stage 4) since their exact method signatures are owned
// by A5's concurrently-written core/api and core/state layers.

import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TicketStore } from '@core/state/ticket.store';
import { AuthStore } from '@core/state/auth.store';
import { TicketApiService } from '@core/api/ticket-api.service';
import { CommentApiService } from '@core/api/comment-api.service';
import { LinkApiService } from '@core/api/link-api.service';
import type {
  CommentNode,
  CreateLinkRequest,
  Priority,
  TicketDetail,
  TicketStatus,
  UserSummary,
} from '@core/models';
import { allowedNext, requiresAssignee, requiresNote, STATUS_LABELS } from '@shared/transitions';
import { MATERIAL_IMPORTS } from '@shared/material';
import { PriorityChipComponent } from '@shared/components/priority-chip.component';
import { StatusBadgeComponent } from '@shared/components/status-badge.component';
import { OverdueBadgeComponent } from '@shared/components/overdue-badge.component';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton.component';
import {
  ConfirmDialogComponent,
  type ConfirmDialogData,
  type ConfirmDialogResult,
} from '@shared/components/confirm-dialog.component';
import { CommentThreadComponent, type ReplySubmitted } from './comment-thread.component';
import { StatusHistoryComponent } from './status-history.component';
import { LinkListComponent } from './link-list.component';

const MOCK_USERS: UserSummary[] = [
  { id: 'u-1', fullName: 'Ada Lovelace', email: 'ada@syncboard.dev', department: 'Platform', active: true },
  { id: 'u-2', fullName: 'Grace Hopper', email: 'grace@syncboard.dev', department: 'Platform', active: true },
  { id: 'u-3', fullName: 'Alan Turing', email: 'alan@syncboard.dev', department: 'Infra', active: true },
];

const MOCK_TICKET: TicketDetail = {
  id: 'mock-ticket',
  title: 'Fix the flaky deploy pipeline',
  status: 'InProgress',
  priority: 'High',
  category: 'Infra',
  description:
    'The nightly deploy job fails intermittently at the DB migration step. Needs root-causing before the next release train.',
  assignee: MOCK_USERS[0],
  reporter: MOCK_USERS[1],
  dueAt: '2026-09-20T00:00:00Z',
  overdue: false,
  commentCount: 2,
  linkCount: 1,
  createdAt: '2026-09-01T09:00:00Z',
  updatedAt: '2026-09-05T14:30:00Z',
  closedAt: undefined,
  blockedFromStatus: undefined,
  version: 3,
  links: [
    {
      id: 'link-1',
      url: 'https://github.com/syncboard/syncboard/pull/42',
      linkTitle: 'PR #42 — retry migration step',
      platform: 'github',
      createdAt: '2026-09-02T10:00:00Z',
    },
  ],
  comments: [
    {
      id: 'c-1',
      bodyText: 'Repro’d locally — looks like a connection-pool exhaustion issue under load.',
      author: MOCK_USERS[1],
      createdAt: '2026-09-02T11:00:00Z',
      editedAt: undefined,
      replies: [
        {
          id: 'c-1-1',
          bodyText: 'Good catch — bumping the pool size in staging now.',
          author: MOCK_USERS[0],
          createdAt: '2026-09-02T12:15:00Z',
          editedAt: undefined,
          replies: [],
        },
      ],
    },
    {
      id: 'c-2',
      bodyText: 'Any update? This is blocking the release checklist.',
      author: MOCK_USERS[2],
      createdAt: '2026-09-04T08:00:00Z',
      editedAt: undefined,
      replies: [],
    },
  ],
  history: [
    {
      id: 'h-1',
      toStatus: 'Open',
      changedBy: MOCK_USERS[1],
      changedAt: '2026-09-01T09:00:00Z',
    },
    {
      id: 'h-2',
      fromStatus: 'Open',
      toStatus: 'Triaging',
      changedBy: MOCK_USERS[0],
      changedAt: '2026-09-01T13:00:00Z',
    },
    {
      id: 'h-3',
      fromStatus: 'Triaging',
      toStatus: 'InProgress',
      changedBy: MOCK_USERS[0],
      changedAt: '2026-09-02T09:30:00Z',
      note: 'Starting investigation.',
    },
  ],
};

function findCommentNode(nodes: CommentNode[], id: string): CommentNode | null {
  for (const node of nodes) {
    if (node.id === id) {
      return node;
    }
    const found = findCommentNode(node.replies, id);
    if (found) {
      return found;
    }
  }
  return null;
}

@Component({
  selector: 'sb-ticket-detail-page',
  standalone: true,
  imports: [
    FormsModule,
    PriorityChipComponent,
    StatusBadgeComponent,
    OverdueBadgeComponent,
    EmptyStateComponent,
    LoadingSkeletonComponent,
    CommentThreadComponent,
    StatusHistoryComponent,
    LinkListComponent,
    ...MATERIAL_IMPORTS,
  ],
  templateUrl: './ticket-detail-page.component.html',
  styleUrl: './ticket-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TicketDetailPageComponent {
  /** Bound from the `:id` route param via withComponentInputBinding() (A5's app.config.ts). */
  readonly id = input.required<string>();

  protected readonly ticketStore = inject(TicketStore);
  protected readonly authStore = inject(AuthStore);
  private readonly ticketApi = inject(TicketApiService);
  private readonly commentApi = inject(CommentApiService);
  private readonly linkApi = inject(LinkApiService);
  private readonly dialog = inject(MatDialog);

  // TODO(stage 4): replace this view-local seed with a load-on-navigate effect once
  // TicketStore's public data signal / TicketApiService.getTicket-equivalent method is
  // finalized by A5, e.g.:
  //   effect(() => { this.ticketApi.getTicket(this.id()).subscribe((t) => this.ticket.set(t)); });
  protected readonly ticket = signal<TicketDetail | null>(MOCK_TICKET);

  protected readonly availableAssignees = MOCK_USERS;
  protected readonly priorities: Priority[] = ['Critical', 'High', 'Medium', 'Low'];

  protected readonly editingTitle = signal(false);
  protected readonly titleDraft = signal('');
  protected readonly editingDescription = signal(false);
  protected readonly descriptionDraft = signal('');

  protected readonly allowedStatuses = computed<TicketStatus[]>(() => {
    const current = this.ticket();
    if (!current) {
      return [];
    }
    return allowedNext(current.status, current.blockedFromStatus ?? null);
  });

  protected readonly dueDateValue = computed<Date | null>(() => {
    const dueAt = this.ticket()?.dueAt;
    return dueAt ? new Date(dueAt) : null;
  });

  protected readonly statusLabels = STATUS_LABELS;

  // --- Title / description inline editing (local — TODO(stage 4): persist via
  // TicketApiService.updateTicket(id, { title/description, version })) ---
  protected startEditTitle(): void {
    this.titleDraft.set(this.ticket()?.title ?? '');
    this.editingTitle.set(true);
  }

  protected saveTitle(): void {
    const title = this.titleDraft().trim();
    if (title) {
      this.ticket.update((t) => (t ? { ...t, title } : t));
    }
    this.editingTitle.set(false);
  }

  protected startEditDescription(): void {
    this.descriptionDraft.set(this.ticket()?.description ?? '');
    this.editingDescription.set(true);
  }

  protected saveDescription(): void {
    this.ticket.update((t) => (t ? { ...t, description: this.descriptionDraft().trim() || undefined } : t));
    this.editingDescription.set(false);
  }

  // --- Status / priority / assignee / due date (local mutation — TODO(stage 4): call the
  // POST /tickets/{id}/transitions and PATCH /tickets/{id} endpoints via TicketApiService) ---
  protected changeStatus(toStatus: TicketStatus): void {
    const current = this.ticket();
    if (!current) {
      return;
    }

    if (requiresAssignee(toStatus) && !current.assignee) {
      this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, ConfirmDialogResult>(ConfirmDialogComponent, {
        data: {
          title: 'Assignee required',
          message: 'Completing a ticket requires an assignee. Pick one from the sidebar first.',
          confirmLabel: 'Got it',
          cancelLabel: 'Dismiss',
        } satisfies ConfirmDialogData,
      });
      return;
    }

    if (requiresNote(toStatus)) {
      const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, ConfirmDialogResult>(ConfirmDialogComponent, {
        data: {
          title: toStatus === 'Cancelled' ? 'Cancel ticket' : 'Mark as Blocked',
          message: `A reason is required to move this ticket to ${this.statusLabels[toStatus]}.`,
          requireNote: true,
          noteLabel: 'Reason',
          confirmLabel: 'Confirm',
          tone: toStatus === 'Cancelled' ? 'warn' : 'primary',
        } satisfies ConfirmDialogData,
      });
      ref.afterClosed().subscribe((result) => {
        if (result?.confirmed) {
          this.applyStatus(toStatus, current.status);
        }
      });
      return;
    }

    this.applyStatus(toStatus, current.status);
  }

  private applyStatus(toStatus: TicketStatus, fromStatus: TicketStatus): void {
    this.ticket.update((t) =>
      t
        ? {
            ...t,
            status: toStatus,
            blockedFromStatus: toStatus === 'Blocked' ? fromStatus : undefined,
          }
        : t,
    );
  }

  protected changePriority(priority: Priority): void {
    this.ticket.update((t) => (t ? { ...t, priority } : t));
  }

  protected changeAssignee(assigneeId: string | null): void {
    const assignee = this.availableAssignees.find((u) => u.id === assigneeId) ?? undefined;
    this.ticket.update((t) => (t ? { ...t, assignee } : t));
  }

  protected changeDueDate(date: Date | null): void {
    this.ticket.update((t) => (t ? { ...t, dueAt: date ? date.toISOString() : undefined } : t));
  }

  // --- Comments (local mutation — TODO(stage 4): POST /tickets/{id}/comments via
  // CommentApiService, then reconcile with the returned CommentNode) ---
  protected readonly newCommentDraft = signal('');

  protected submitRootComment(): void {
    this.addRootComment(this.newCommentDraft());
    this.newCommentDraft.set('');
  }

  private addRootComment(bodyText: string): void {
    const text = bodyText.trim();
    if (!text) {
      return;
    }
    const me = this.authStore.currentUser();
    const node: CommentNode = {
      id: `local-${Date.now()}`,
      bodyText: text,
      author: me ?? this.availableAssignees[0],
      createdAt: new Date().toISOString(),
      replies: [],
    };
    this.ticket.update((t) => (t ? { ...t, comments: [...t.comments, node], commentCount: t.commentCount + 1 } : t));
  }

  protected addReply(event: ReplySubmitted): void {
    const text = event.bodyText.trim();
    if (!text) {
      return;
    }
    this.ticket.update((t) => {
      if (!t) {
        return t;
      }
      const comments = structuredClone(t.comments);
      const parent = findCommentNode(comments, event.parentCommentId);
      if (!parent) {
        return t;
      }
      const me = this.authStore.currentUser();
      parent.replies.push({
        id: `local-${Date.now()}`,
        bodyText: text,
        author: me ?? this.availableAssignees[0],
        createdAt: new Date().toISOString(),
        replies: [],
      });
      return { ...t, comments, commentCount: t.commentCount + 1 };
    });
  }

  // --- Links (local mutation — TODO(stage 4): POST/DELETE via LinkApiService) ---
  protected addLink(request: CreateLinkRequest): void {
    this.ticket.update((t) =>
      t
        ? {
            ...t,
            links: [
              ...t.links,
              {
                id: `local-${Date.now()}`,
                url: request.url,
                linkTitle: request.linkTitle,
                platform: request.platform ?? 'other',
                createdAt: new Date().toISOString(),
              },
            ],
            linkCount: t.linkCount + 1,
          }
        : t,
    );
  }

  protected removeLink(linkId: string): void {
    this.ticket.update((t) =>
      t ? { ...t, links: t.links.filter((l) => l.id !== linkId), linkCount: Math.max(0, t.linkCount - 1) } : t,
    );
  }
}
