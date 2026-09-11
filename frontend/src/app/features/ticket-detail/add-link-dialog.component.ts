// Stage 4 — Angular 22 feature screens
// MatDialog form for CreateLinkRequest (contract §4). `platform` is left optional in the
// form because the backend derives it from the URL host when omitted ("Auto-detect").

import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import type { CreateLinkRequest, LinkPlatform } from '@core/models';
import { MATERIAL_IMPORTS } from '@shared/material';

const PLATFORM_OPTIONS: { value: LinkPlatform; label: string }[] = [
  { value: 'keka', label: 'Keka' },
  { value: 'google-drive', label: 'Google Drive' },
  { value: 'google-docs', label: 'Google Docs' },
  { value: 'github', label: 'GitHub' },
  { value: 'slack', label: 'Slack' },
  { value: 'other', label: 'Other' },
];

@Component({
  selector: 'sb-add-link-dialog',
  standalone: true,
  imports: [FormsModule, ...MATERIAL_IMPORTS],
  templateUrl: './add-link-dialog.component.html',
  styleUrl: './add-link-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddLinkDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<AddLinkDialogComponent, CreateLinkRequest>);

  protected readonly platformOptions = PLATFORM_OPTIONS;

  protected readonly url = signal('');
  protected readonly linkTitle = signal('');
  protected readonly platform = signal<LinkPlatform | null>(null);

  protected readonly urlValid = computed(() => {
    const value = this.url().trim();
    if (!value) {
      return false;
    }
    try {
      // eslint-disable-next-line no-new
      new URL(value);
      return true;
    } catch {
      return false;
    }
  });

  protected cancel(): void {
    this.dialogRef.close();
  }

  protected submit(): void {
    if (!this.urlValid()) {
      return;
    }
    const request: CreateLinkRequest = {
      url: this.url().trim(),
      linkTitle: this.linkTitle().trim() || undefined,
      platform: this.platform() ?? undefined,
    };
    this.dialogRef.close(request);
  }
}
