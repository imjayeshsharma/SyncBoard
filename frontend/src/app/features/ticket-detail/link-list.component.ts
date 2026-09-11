// Stage 4 — Angular 22 feature screens
// External linking module (PRD "Resource Hub"): lists TicketLink[] with a platform icon,
// title and URL plus a delete action, and opens add-link-dialog to create a new one.
// Persistence is the page's job (LinkApiService/TicketStore) — this component only emits.

import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import type { CreateLinkRequest, TicketLink } from '@core/models';
import { PlatformIconPipe } from '@shared/pipes/platform-icon.pipe';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { MATERIAL_IMPORTS } from '@shared/material';
import { AddLinkDialogComponent } from './add-link-dialog.component';

@Component({
  selector: 'sb-link-list',
  standalone: true,
  imports: [PlatformIconPipe, EmptyStateComponent, ...MATERIAL_IMPORTS],
  templateUrl: './link-list.component.html',
  styleUrl: './link-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LinkListComponent {
  private readonly dialog = inject(MatDialog);

  readonly links = input.required<TicketLink[]>();

  readonly linkAdded = output<CreateLinkRequest>();
  readonly linkRemoved = output<string>();

  protected openAddDialog(): void {
    const ref = this.dialog.open(AddLinkDialogComponent);
    ref.afterClosed().subscribe((result?: CreateLinkRequest) => {
      if (result) {
        this.linkAdded.emit(result);
      }
    });
  }

  protected remove(linkId: string): void {
    this.linkRemoved.emit(linkId);
  }
}
