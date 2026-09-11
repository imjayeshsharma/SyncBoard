// Stage 4 — Angular 22 feature screens
// Renders a TicketStatus as its PRD board-column label (contract §2), colour-coded.

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import type { TicketStatus } from '@core/models';
import { STATUS_LABELS } from '@shared/transitions';

@Component({
  selector: 'sb-status-badge',
  standalone: true,
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  readonly status = input.required<TicketStatus>();

  protected readonly label = computed(() => STATUS_LABELS[this.status()]);
}
