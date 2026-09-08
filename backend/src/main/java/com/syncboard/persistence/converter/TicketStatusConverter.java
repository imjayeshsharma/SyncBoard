// Stage 3 — PostgreSQL 18 persistence
package com.syncboard.persistence.converter;

import com.syncboard.domain.TicketStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link TicketStatus} as its wire value (e.g. {@code "InProgress"}), never as an
 * ordinal or a Postgres enum type — see 01-CONTRACT.md §3. Applied explicitly via
 * {@code @Convert}, not {@code @Enumerated}.
 */
@Converter(autoApply = false)
public class TicketStatusConverter implements AttributeConverter<TicketStatus, String> {

    @Override
    public String convertToDatabaseColumn(TicketStatus attribute) {
        return attribute == null ? null : attribute.getWireValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TicketStatus.fromWireValue(dbData);
    }
}
