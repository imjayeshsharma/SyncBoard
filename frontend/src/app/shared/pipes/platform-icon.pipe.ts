// Stage 4 — Angular 22 feature screens
// Maps a resource link's `LinkPlatform` (contract §4) to a Material icon ligature name,
// used by link-list.component to render a platform glyph next to each linked resource.

import { Pipe, PipeTransform } from '@angular/core';
import type { LinkPlatform } from '@core/models';

const ICON_MAP: Record<LinkPlatform, string> = {
  keka: 'badge',
  'google-drive': 'folder_shared',
  'google-docs': 'description',
  github: 'code',
  slack: 'forum',
  other: 'link',
};

@Pipe({
  name: 'platformIcon',
  standalone: true,
})
export class PlatformIconPipe implements PipeTransform {
  transform(platform: LinkPlatform | null | undefined): string {
    if (!platform) {
      return 'link';
    }
    return ICON_MAP[platform] ?? 'link';
  }
}
