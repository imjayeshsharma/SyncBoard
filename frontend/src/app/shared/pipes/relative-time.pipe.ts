// Stage 4 — Angular 22 feature screens
// Template-friendly wrapper around a relative-time formatter, used by comment threads,
// status history entries, etc. (e.g. "3h ago", "just now").

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'relativeTime',
  standalone: true,
  pure: false, // re-evaluates on each CD run so "3m ago" keeps advancing
})
export class RelativeTimePipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    const diffMs = Date.now() - date.getTime();
    const diffSec = Math.round(diffMs / 1000);
    const abs = Math.abs(diffSec);

    if (abs < 5) {
      return 'just now';
    }
    const units: [number, string][] = [
      [60, 'second'],
      [60, 'minute'],
      [24, 'hour'],
      [7, 'day'],
      [4.345, 'week'],
      [12, 'month'],
      [Number.POSITIVE_INFINITY, 'year'],
    ];
    let value2 = abs;
    let unitLabel = 'second';
    for (const [span, label] of units) {
      if (value2 < span) {
        unitLabel = label;
        break;
      }
      value2 = Math.floor(value2 / span);
      unitLabel = label;
    }
    const rounded = Math.max(1, Math.floor(value2));
    const plural = rounded === 1 ? '' : 's';
    return diffSec >= 0 ? `${rounded} ${unitLabel}${plural} ago` : `in ${rounded} ${unitLabel}${plural}`;
  }
}
