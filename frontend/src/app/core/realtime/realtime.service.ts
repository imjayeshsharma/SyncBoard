// Stage 4 — Angular 22 shell

import { Injectable, signal, type Signal } from '@angular/core';

import type { BoardEvent, CommentNode } from '@core/models';
import { environment } from '@env/environment';

/**
 * Minimal seam for a STOMP-over-WebSocket client (contract §4 "Realtime").
 * `package.json` intentionally carries no STOMP library yet, so this
 * declares the shape a future `@stomp/stompjs`-backed implementation must
 * satisfy without introducing that dependency here.
 */
export interface StompLikeClient {
  activate(): void;
  deactivate(): void;
  subscribe(destination: string, callback: (body: string) => void): { unsubscribe(): void };
}

/**
 * STOMP client seam over `environment.wsUrl`. Connection + (de)serialisation
 * is TODO(stage 6); the public signal-based surface below is final and is
 * what board/ticket-detail features should depend on.
 */
@Injectable({ providedIn: 'root' })
export class RealtimeService {
  private readonly wsUrl = environment.wsUrl;
  private client: StompLikeClient | null = null;

  private readonly _boardEvents = signal<BoardEvent | null>(null);
  /** Latest message observed on `/topic/board`. */
  readonly boardEvents: Signal<BoardEvent | null> = this._boardEvents.asReadonly();

  private readonly commentSignals = new Map<string, ReturnType<typeof signal<CommentNode | null>>>();

  /** Connects the underlying STOMP client. TODO(stage 6): wire a real client to `wsUrl`. */
  connect(): void {
    // TODO(stage 6): construct a StompLikeClient pointed at this.wsUrl and call activate().
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
  }

  /** Latest message observed on `/topic/tickets/{ticketId}/comments`. */
  commentEvents(ticketId: string): Signal<CommentNode | null> {
    let sig = this.commentSignals.get(ticketId);
    if (!sig) {
      sig = signal<CommentNode | null>(null);
      this.commentSignals.set(ticketId, sig);
      // TODO(stage 6): subscribe to `/topic/tickets/${ticketId}/comments` and push into sig.
    }
    return sig.asReadonly();
  }
}
