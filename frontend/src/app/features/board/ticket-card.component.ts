// Stage 4 — Angular 22 feature screens
// A single Kanban card: priority stripe, title, category, assignee, due date and an
// overdue marker (PRD "Global Kanban Board"). Presentational — no store access; the
// hosting board-column supplies the `cdkDrag` wrapper and drag data.

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import type { TicketSummary } from '@core/models';
import { PriorityChipComponent } from '@shared/components/priority-chip.component';
import { OverdueBadgeComponent } from '@shared/components/overdue-badge.component';
import { UserAvatarComponent } from '@shared/components/user-avatar.component';

@Component({
  selector: 'sb-ticket-card',
  standalone: true,
  imports: [RouterLink, MatIconModule, PriorityChipComponent, OverdueBadgeComponent, UserAvatarComponent],
  templateUrl: './ticket-card.component.html',
  styleUrl: './ticket-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TicketCardComponent {
  readonly ticket = input.required<TicketSummary>();

  protected readonly dueLabel = computed(() => {
    const dueAt = this.ticket().dueAt;
    if (!dueAt) {
      return null;
    }
    return new Date(dueAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  });
}
