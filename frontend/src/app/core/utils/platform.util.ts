// Stage 4 — Angular 22 shell

import type { LinkPlatform } from '@core/models';

/**
 * Client-side mirror of the server-side platform derivation described in
 * contract §4 ("`platform` is derived server-side from the URL host when the
 * client omits it"). Used for local preview before the create/edit request
 * round-trips, and to pick a Material icon for a given platform.
 */
export function platformFromUrl(url: string): LinkPlatform {
  let host = '';
  try {
    host = new URL(url).hostname.toLowerCase();
  } catch {
    return 'other';
  }

  if (host.includes('keka.com')) return 'keka';
  if (host.includes('docs.google.com')) return 'google-docs';
  if (host.includes('drive.google.com')) return 'google-drive';
  if (host.includes('github.com')) return 'github';
  if (host.includes('slack.com')) return 'slack';
  return 'other';
}

const PLATFORM_ICONS: Record<LinkPlatform, string> = {
  keka: 'badge',
  'google-drive': 'drive_file_move',
  'google-docs': 'description',
  github: 'code',
  slack: 'forum',
  other: 'link',
};

export function iconForPlatform(platform: LinkPlatform): string {
  return PLATFORM_ICONS[platform] ?? 'link';
}
