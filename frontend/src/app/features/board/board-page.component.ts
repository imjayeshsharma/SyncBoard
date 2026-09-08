// Stage 4 — Angular 22 feature screens
// PRD "Global Kanban Board" — route target for '/board'. Six status columns wired to
// @angular/cdk/drag-drop; filtering is done client-side over BoardStore's data via
// view-local filter signals + a computed(). BoardStore owns the optimistic
// move-with-rollback; this page only computes source/target status from the raw CDK event.

import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { CdkDropListGroup, type CdkDragDrop } from '@angular/cdk/drag-drop';
import { MatDialog } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { BoardStore } from '@core/state/board.store';
import { AuthStore } from '@core/state/auth.store';
import type { BoardColumn, Priority, TicketStatus, TicketSummary, UserSummary } from '@core/models';
import { requiresNote, STATUS_LABELS, toOrderedColumns } from '@shared/transitions';
import { MATERIAL_IMPORTS } from '@shared/material';
import { BoardColumnComponent } from './board-column.component';
import {
  ConfirmDialogComponent,
  type ConfirmDialogData,
  type ConfirmDialogResult,
} from '@shared/components/confirm-dialog.component';

const PRIORITIES: Priority[] = ['Critical', 'High', 'Medium', 'Low'];

@Component({
  selector: 'sb-board-page',
  standalone: true,
  imports: [FormsModule, CdkDropListGroup, BoardColumnComponent, ...MATERIAL_IMPORTS],
  templateUrl: './board-page.component.html',
  styleUrl: './board-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoardPageComponent {
  protected readonly boardStore = inject(BoardStore);
  protected readonly authStore = inject(AuthStore);
  private readonly dialog = inject(MatDialog);

  protected readonly priorities = PRIORITIES;

  // --- view-local filter state (signals) ---
  protected readonly search = signal('');
  protected readonly priorityFilter = signal<Priority | 'all'>('all');
  protected readonly assigneeFilter = signal<string | 'all'>('all');
  protected readonly myTicketsOnly = signal(false);

  protected readonly availableAssignees = computed<UserSummary[]>(() => {
    const map = new Map<string, UserSummary>();
    for (const column of this.boardStore.columns()) {
      for (const ticket of column.tickets) {
        if (ticket.assignee) {
          map.set(ticket.assignee.id, ticket.assignee);
        }
      }
    }
    return [...map.values()];
  });

  protected readonly filteredColumns = computed<BoardColumn[]>(() => {
    const term = this.search().trim().toLowerCase();
    const priority = this.priorityFilter();
    const assigneeId = this.assigneeFilter();
    const mine = this.myTicketsOnly();
    const me = this.authStore.currentUser();

    return toOrderedColumns(this.boardStore.columns()).map((column) => ({
      ...column,
      tickets: column.tickets.filter((ticket) => {
        if (term && !ticket.title.toLowerCase().includes(term)) {
          return false;
        }
        if (priority !== 'all' && ticket.priority !== priority) {
          return false;
        }
        if (assigneeId !== 'all' && ticket.assignee?.id !== assigneeId) {
          return false;
        }
        if (mine && ticket.assignee?.id !== me?.id) {
          return false;
        }
        return true;
      }),
    }));
  });

  protected readonly overdueCount = computed(() =>
    this.filteredColumns().reduce(
      (sum, column) => sum + column.tickets.filter((ticket) => ticket.overdue).length,
      0,
    ),
  );

  protected onDrop(event: CdkDragDrop<TicketSummary[]>): void {
    if (event.previousContainer === event.container) {
      // TODO(stage 4): persist in-column reordering once the backend supports a rank field.
      return;
    }
    const ticket = event.item.data as TicketSummary;
    const toStatus = this.statusFromDropListId(event.container.id);
    if (!toStatus || ticket.status === toStatus) {
      return;
    }

    if (requiresNote(toStatus)) {
      const ref = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, ConfirmDialogResult>(ConfirmDialogComponent, {
        data: {
          title: toStatus === 'Cancelled' ? 'Cancel ticket' : 'Mark as Blocked',
          message: `A reason is required to move "${ticket.title}" to ${STATUS_LABELS[toStatus]}.`,
          requireNote: true,
          noteLabel: 'Reason',
          confirmLabel: 'Move',
          tone: toStatus === 'Cancelled' ? 'warn' : 'primary',
        } satisfies ConfirmDialogData,
      });
      ref.afterClosed().subscribe((result) => {
        if (result?.confirmed) {
          this.boardStore.moveTicket(ticket.id, toStatus, result.note);
        }
      });
      return;
    }

    this.boardStore.moveTicket(ticket.id, toStatus);
  }

  private statusFromDropListId(id: string): TicketStatus | null {
    const prefix = 'board-column-';
    return id.startsWith(prefix) ? (id.slice(prefix.length) as TicketStatus) : null;
  }
}
