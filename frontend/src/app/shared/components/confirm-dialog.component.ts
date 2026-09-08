// Stage 4 — Angular 22 feature screens
// Generic MatDialog confirmation, with an optional required-note field so it can satisfy
// the backend's REASON_REQUIRED (-> Cancelled / -> Blocked) rule from contract §2.
// Usage: dialog.open(ConfirmDialogComponent, { data: { ... } as ConfirmDialogData })
//   .afterClosed() resolves to `ConfirmDialogResult | undefined` (undefined = dismissed).

import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MATERIAL_IMPORTS } from '@shared/material';

export interface ConfirmDialogData {
  title: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  /** When true, a note textarea is shown and confirm is disabled until it's non-blank. */
  requireNote?: boolean;
  noteLabel?: string;
  /** Visual tone for the confirm button. */
  tone?: 'primary' | 'warn';
}

export interface ConfirmDialogResult {
  confirmed: true;
  note?: string;
}

@Component({
  selector: 'sb-confirm-dialog',
  standalone: true,
  imports: [FormsModule, ...MATERIAL_IMPORTS],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDialogComponent {
  protected readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ConfirmDialogComponent, ConfirmDialogResult>);

  protected readonly note = signal('');
  protected readonly canConfirm = computed(() => !this.data.requireNote || this.note().trim().length > 0);

  protected cancel(): void {
    this.dialogRef.close();
  }

  protected confirm(): void {
    if (!this.canConfirm()) {
      return;
    }
    this.dialogRef.close({ confirmed: true, note: this.note().trim() || undefined });
  }
}
