// Stage 4 — Angular 22 feature screens
// Recursive rendering of a CommentNode + its `replies` tree (contract §4 CommentNode is
// self-referential). Each node shows an author avatar, a relative timestamp and a reply
// box; replies bubble up through `submitReply` unchanged regardless of nesting depth, so
// the page component only ever needs one handler.

import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { CommentNode } from '@core/models';
import { UserAvatarComponent } from '@shared/components/user-avatar.component';
import { RelativeTimePipe } from '@shared/pipes/relative-time.pipe';
import { MATERIAL_IMPORTS } from '@shared/material';

export interface ReplySubmitted {
  parentCommentId: string;
  bodyText: string;
}

@Component({
  selector: 'sb-comment-thread',
  standalone: true,
  imports: [FormsModule, UserAvatarComponent, RelativeTimePipe, ...MATERIAL_IMPORTS, CommentThreadComponent],
  templateUrl: './comment-thread.component.html',
  styleUrl: './comment-thread.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CommentThreadComponent {
  readonly comment = input.required<CommentNode>();
  readonly depth = input(0);

  readonly submitReply = output<ReplySubmitted>();

  protected readonly replyOpen = signal(false);
  protected readonly draft = signal('');

  protected toggleReply(): void {
    this.replyOpen.update((open) => !open);
  }

  protected sendReply(): void {
    const bodyText = this.draft().trim();
    if (!bodyText) {
      return;
    }
    this.submitReply.emit({ parentCommentId: this.comment().id, bodyText });
    this.draft.set('');
    this.replyOpen.set(false);
  }

  protected onNestedReply(event: ReplySubmitted): void {
    this.submitReply.emit(event);
  }
}
