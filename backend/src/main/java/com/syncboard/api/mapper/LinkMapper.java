// Stage 2 — REST API mappers
package com.syncboard.api.mapper;

import com.syncboard.api.dto.TicketLinkDto;
import com.syncboard.persistence.entity.TicketLinkEntity;
import org.springframework.stereotype.Component;

/**
 * Entity to DTO mapping for {@link TicketLinkEntity} -&gt; {@link TicketLinkDto}.
 *
 * <p>Assumes {@code TicketLinkEntity} exposes {@code getId, getUrl, getLinkTitle, getPlatform,
 * getCreatedAt} — A2 owns this entity and had not landed it at the time this file was written.
 */
@Component
public class LinkMapper {

    public TicketLinkDto toDto(TicketLinkEntity entity) {
        return new TicketLinkDto(
                entity.getId(),
                entity.getUrl(),
                entity.getLinkTitle(),
                entity.getPlatform(),
                entity.getCreatedAt()
        );
    }
}
