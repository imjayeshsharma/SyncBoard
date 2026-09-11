// Stage 4 — Angular 22 feature screens
// Small red "Overdue" marker shown on ticket cards / detail sidebar per PRD.
// Renders nothing when `overdue` is false, so callers can bind it unconditionally.

import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'sb-overdue-badge',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './overdue-badge.component.html',
  styleUrl: './overdue-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverdueBadgeComponent {
  readonly overdue = input(false);
}
