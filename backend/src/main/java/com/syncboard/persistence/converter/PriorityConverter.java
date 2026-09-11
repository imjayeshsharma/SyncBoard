// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.converter;

import com.syncboard.domain.Priority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link Priority} as its wire value (e.g. {@code "High"}), never as an ordinal or a
 * Postgres enum type — see 01-CONTRACT.md §3. Applied explicitly via {@code @Convert}, not
 * {@code @Enumerated}.
 */
@Converter(autoApply = false)
public class PriorityConverter implements AttributeConverter<Priority, String> {

    @Override
    public String convertToDatabaseColumn(Priority attribute) {
        return attribute == null ? null : attribute.getWireValue();
    }

    @Override
    public Priority convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Priority.fromWireValue(dbData);
    }
}
