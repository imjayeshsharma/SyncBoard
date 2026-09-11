// Stage 4 — Angular 22 feature screens
// Generic empty-state placeholder: icon + title + message + optional action button.
// Used for empty board columns, empty comment threads, empty link lists, etc.

import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MATERIAL_IMPORTS } from '@shared/material';

@Component({
  selector: 'sb-empty-state',
  standalone: true,
  imports: [...MATERIAL_IMPORTS],
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyStateComponent {
  readonly icon = input('inbox');
  readonly title = input('Nothing here yet');
  readonly message = input<string | null>(null);
  readonly actionLabel = input<string | null>(null);

  readonly action = output<void>();
}
