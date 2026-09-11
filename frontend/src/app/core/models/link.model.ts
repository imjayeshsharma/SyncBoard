// Stage 4 — Angular 22 shell
// Mirrors contract §4: TicketLink, CreateLinkRequest, and the derived platform union.

export type LinkPlatform =
  | 'keka'
  | 'google-drive'
  | 'google-docs'
  | 'github'
  | 'slack'
  | 'other';

export interface TicketLink {
  id: string;
  url: string;
  linkTitle?: string;
  platform: LinkPlatform;
  createdAt: string;
}

export interface CreateLinkRequest {
  url: string;
  linkTitle?: string;
  platform?: LinkPlatform;
}
