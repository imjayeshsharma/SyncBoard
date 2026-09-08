// Stage 4 — Angular 22 feature screens
// Small circular initials avatar for a UserSummary, with an optional name label.
// Used by ticket-card, app toolbar (via A5), comment-thread, assignee picker.

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import type { UserSummary } from '@core/models';

@Component({
  selector: 'sb-user-avatar',
  standalone: true,
  templateUrl: './user-avatar.component.html',
  styleUrl: './user-avatar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserAvatarComponent {
  readonly user = input<UserSummary | null>(null);
  /** Show the full name next to the avatar circle. */
  readonly showName = input(false);
  /** Diameter in pixels. */
  readonly size = input(28);

  protected readonly initials = computed(() => {
    const name = this.user()?.fullName?.trim();
    if (!name) {
      return '?';
    }
    const parts = name.split(/\s+/).filter(Boolean);
    const first = parts[0]?.[0] ?? '';
    const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
    return (first + last).toUpperCase();
  });
}
