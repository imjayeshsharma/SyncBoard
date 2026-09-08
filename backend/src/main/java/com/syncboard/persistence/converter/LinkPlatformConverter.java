// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.converter;

import com.syncboard.domain.LinkPlatform;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link LinkPlatform} as its wire value (e.g. {@code "google-drive"}), never as an
 * ordinal or a Postgres enum type — see 01-CONTRACT.md §3/§4. Applied explicitly via
 * {@code @Convert}, not {@code @Enumerated}.
 */
@Converter(autoApply = false)
public class LinkPlatformConverter implements AttributeConverter<LinkPlatform, String> {

    @Override
    public String convertToDatabaseColumn(LinkPlatform attribute) {
        return attribute == null ? null : attribute.getWireValue();
    }

    @Override
    public LinkPlatform convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LinkPlatform.fromWireValue(dbData);
    }
}
