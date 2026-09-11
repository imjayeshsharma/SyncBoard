// Stage 4 — Angular 22 feature screens
// Collapsed activity trail (PRD "status history") — a mat-expansion-panel listing every
// StatusHistoryEntry: from -> to, who changed it, when, and the optional note.

import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import type { StatusHistoryEntry } from '@core/models';
import { StatusBadgeComponent } from '@shared/components/status-badge.component';
import { UserAvatarComponent } from '@shared/components/user-avatar.component';
import { RelativeTimePipe } from '@shared/pipes/relative-time.pipe';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { MATERIAL_IMPORTS } from '@shared/material';

@Component({
  selector: 'sb-status-history',
  standalone: true,
  imports: [StatusBadgeComponent, UserAvatarComponent, RelativeTimePipe, EmptyStateComponent, ...MATERIAL_IMPORTS],
  templateUrl: './status-history.component.html',
  styleUrl: './status-history.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusHistoryComponent {
  readonly history = input.required<StatusHistoryEntry[]>();
}
