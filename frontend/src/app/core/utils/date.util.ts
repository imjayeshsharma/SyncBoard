// Stage 4 — Angular 22 shell

import type { TicketStatus } from '@core/models';

/**
 * Mirrors the contract §3 derived `overdue` rule:
 * `dueAt != null && dueAt.isBefore(now) && status not in (COMPLETED, CANCELLED)`.
 */
export function isOverdue(dueAt: string | null | undefined, status: TicketStatus): boolean {
  if (!dueAt) {
    return false;
  }
  if (status === 'Completed' || status === 'Cancelled') {
    return false;
  }
  return new Date(dueAt).getTime() < Date.now();
}

/** Formats an ISO instant as a short relative-time string, e.g. "3h ago" / "in 2d". */
export function formatRelative(instant: string | null | undefined): string {
  if (!instant) {
    return '';
  }
  const then = new Date(instant).getTime();
  if (Number.isNaN(then)) {
    return '';
  }
  const diffMs = then - Date.now();
  const diffSec = Math.round(diffMs / 1000);
  const abs = Math.abs(diffSec);

  const units: [Intl.RelativeTimeFormatUnit, number][] = [
    ['year', 60 * 60 * 24 * 365],
    ['month', 60 * 60 * 24 * 30],
    ['week', 60 * 60 * 24 * 7],
    ['day', 60 * 60 * 24],
    ['hour', 60 * 60],
    ['minute', 60],
    ['second', 1],
  ];

  const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });
  for (const [unit, secondsInUnit] of units) {
    if (abs >= secondsInUnit || unit === 'second') {
      const value = Math.round(diffSec / secondsInUnit);
      return rtf.format(value, unit);
    }
  }
  return '';
}
