// Stage 1 — Java 25 domain core
package com.syncboard.domain;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The external platform a {@code TicketLink} points to, inferred from the link's URL
 * host when the client does not supply it explicitly (01-CONTRACT.md §4).
 */
public enum LinkPlatform {
    KEKA("keka"),
    GOOGLE_DRIVE("google-drive"),
    GOOGLE_DOCS("google-docs"),
    GITHUB("github"),
    SLACK("slack"),
    OTHER("other");

    private final String wireValue;

    LinkPlatform(String wireValue) {
        this.wireValue = wireValue;
    }

    /** The JSON/DB/TS wire value for this platform. */
    public String getWireValue() {
        return wireValue;
    }

    /** Resolves a platform from its wire value; throws if unknown. */
    public static LinkPlatform fromWireValue(String wireValue) {
        for (LinkPlatform platform : values()) {
            if (platform.wireValue.equals(wireValue)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown LinkPlatform wire value: " + wireValue);
    }

    /**
     * Infers the platform from a URL's host. Falls back to {@code OTHER} for any
     * unrecognised or malformed URL — never throws.
     */
    public static LinkPlatform fromUrl(String url) {
        if (url == null || url.isBlank()) {
            return OTHER;
        }
        try {
            String host = new URI(url.trim()).getHost();
            if (host == null) {
                return OTHER;
            }
            host = host.toLowerCase();
            if (host.equals("keka.com") || host.endsWith(".keka.com")) {
                return KEKA;
            }
            if (host.equals("drive.google.com") || host.endsWith(".drive.google.com")) {
                return GOOGLE_DRIVE;
            }
            if (host.equals("docs.google.com") || host.endsWith(".docs.google.com")) {
                return GOOGLE_DOCS;
            }
            if (host.equals("github.com") || host.endsWith(".github.com")) {
                return GITHUB;
            }
            if (host.equals("slack.com") || host.endsWith(".slack.com")) {
                return SLACK;
            }
            return OTHER;
        } catch (URISyntaxException | IllegalArgumentException e) {
            return OTHER;
        }
    }
}
