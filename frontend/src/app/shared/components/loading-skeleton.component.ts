// Stage 4 — Angular 22 feature screens
// Shimmering placeholder block(s) shown while board/ticket data is loading.

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'sb-loading-skeleton',
  standalone: true,
  templateUrl: './loading-skeleton.component.html',
  styleUrl: './loading-skeleton.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadingSkeletonComponent {
  /** Number of skeleton rows/cards to render. */
  readonly rows = input(3);
  /** Height of each row, in pixels. */
  readonly rowHeight = input(84);

  protected readonly rowIndexes = computed(() => Array.from({ length: this.rows() }, (_, i) => i));
}
