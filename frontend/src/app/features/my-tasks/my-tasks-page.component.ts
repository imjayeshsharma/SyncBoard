// Stage 4 — Angular 22 feature screens
// "My Tasks" — the same six-column board, filtered to the signed-in user's assignments via
// a computed() over BoardStore + AuthStore. Reuses board-column/ticket-card from
// features/board — no duplicated column/card layout.

import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { CdkDropListGroup, type CdkDragDrop } from '@angular/cdk/drag-drop';
import { BoardStore } from '@core/state/board.store';
import { AuthStore } from '@core/state/auth.store';
import type { BoardColumn, TicketStatus, TicketSummary } from '@core/models';
import { BoardColumnComponent } from '@features/board/board-column.component';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { toOrderedColumns } from '@shared/transitions';

@Component({
  selector: 'sb-my-tasks-page',
  standalone: true,
  imports: [CdkDropListGroup, BoardColumnComponent, EmptyStateComponent],
  templateUrl: './my-tasks-page.component.html',
  styleUrl: './my-tasks-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyTasksPageComponent {
  protected readonly boardStore = inject(BoardStore);
  protected readonly authStore = inject(AuthStore);

  protected readonly myColumns = computed<BoardColumn[]>(() => {
    const me = this.authStore.currentUser();
    return toOrderedColumns(this.boardStore.columns()).map((column) => ({
      ...column,
      tickets: column.tickets.filter((ticket) => ticket.assignee?.id === me?.id),
    }));
  });

  protected readonly hasAnyTickets = computed(() =>
    this.myColumns().some((column) => column.tickets.length > 0),
  );

  protected onDrop(event: CdkDragDrop<TicketSummary[]>): void {
    if (event.previousContainer === event.container) {
      return;
    }
    const ticket = event.item.data as TicketSummary;
    const toStatus = this.statusFromDropListId(event.container.id);
    if (!toStatus || ticket.status === toStatus) {
      return;
    }
    this.boardStore.moveTicket(ticket.id, toStatus);
  }

  private statusFromDropListId(id: string): TicketStatus | null {
    const prefix = 'board-column-';
    return id.startsWith(prefix) ? (id.slice(prefix.length) as TicketStatus) : null;
  }
}
