// Stage 4 — Angular 22 feature screens
// Small colour-coded chip for a ticket's Priority (contract §2 wire values).

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import type { Priority } from '@core/models';

@Component({
  selector: 'sb-priority-chip',
  standalone: true,
  templateUrl: './priority-chip.component.html',
  styleUrl: './priority-chip.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PriorityChipComponent {
  readonly priority = input.required<Priority>();

  protected readonly cssVar = computed(() => `--sb-priority-${this.priority().toLowerCase()}`);
}
