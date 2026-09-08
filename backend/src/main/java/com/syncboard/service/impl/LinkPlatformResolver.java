// Stage 2/3 — service layer
package com.syncboard.service.impl;

import com.syncboard.domain.LinkPlatform;

import java.net.URI;
import java.util.Locale;

/**
 * Derives {@link LinkPlatform} from a URL's host when the client omits it
 * (contract 01-CONTRACT.md §4: "platform is derived server-side from the URL host").
 * Package-private: shared by {@link TicketServiceImpl} (links on ticket creation) and
 * {@link TicketLinkServiceImpl} (the dedicated links endpoint).
 *
 * <p>Assumes {@link LinkPlatform} has constants {@code KEKA, GOOGLE_DRIVE, GOOGLE_DOCS,
 * GITHUB, SLACK, OTHER} mirroring the wire values in contract §4 — A1 owns this enum and had
 * not landed it at the time this file was written.
 */
final class LinkPlatformResolver {

    private LinkPlatformResolver() {
    }

    static LinkPlatform resolve(String url) {
        if (url == null) {
            return LinkPlatform.OTHER;
        }
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (IllegalArgumentException malformed) {
            return LinkPlatform.OTHER;
        }
        if (host == null) {
            return LinkPlatform.OTHER;
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.contains("keka")) {
            return LinkPlatform.KEKA;
        }
        if (host.contains("docs.google.com")) {
            return LinkPlatform.GOOGLE_DOCS;
        }
        if (host.contains("drive.google.com")) {
            return LinkPlatform.GOOGLE_DRIVE;
        }
        if (host.contains("github.com")) {
            return LinkPlatform.GITHUB;
        }
        if (host.contains("slack.com")) {
            return LinkPlatform.SLACK;
        }
        return LinkPlatform.OTHER;
    }
}
