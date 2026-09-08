// Stage 4 — Angular 22 feature screens
// One Kanban column: header (label + count) and a cdkDropList drop target. The parent
// board-page.component wraps all instances in a single cdkDropListGroup and owns the
// actual move logic — this component only forwards the raw CdkDragDrop event upward.

import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { CdkDropList, CdkDrag, type CdkDragDrop } from '@angular/cdk/drag-drop';
import type { TicketStatus, TicketSummary } from '@core/models';
import { STATUS_LABELS } from '@shared/transitions';
import { TicketCardComponent } from './ticket-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton.component';

@Component({
  selector: 'sb-board-column',
  standalone: true,
  imports: [CdkDropList, CdkDrag, TicketCardComponent, EmptyStateComponent, LoadingSkeletonComponent],
  templateUrl: './board-column.component.html',
  styleUrl: './board-column.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoardColumnComponent {
  readonly status = input.required<TicketStatus>();
  readonly tickets = input.required<TicketSummary[]>();
  readonly loading = input(false);

  readonly dropped = output<CdkDragDrop<TicketSummary[]>>();

  protected readonly label = computed(() => STATUS_LABELS[this.status()]);
  protected readonly dropListId = computed(() => `board-column-${this.status()}`);

  protected onDrop(event: CdkDragDrop<TicketSummary[]>): void {
    this.dropped.emit(event);
  }
}
