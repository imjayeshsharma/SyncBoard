// Stage 2 — REST API JSON binding
package com.syncboard.api.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.syncboard.domain.LinkPlatform;
import com.syncboard.domain.Priority;
import com.syncboard.domain.TicketStatus;

import java.io.IOException;

/**
 * The single place enum JSON wire-format is decided (contract 01-CONTRACT.md §2, §4).
 * {@link TicketStatus}, {@link Priority} and {@link LinkPlatform} all serialise to and
 * parse from their {@code wireValue} (e.g. {@code IN_PROGRESS <-> "InProgress"}), never
 * their Java constant name.
 *
 * <p>Registered as a Spring bean by {@link JacksonConfig}; Spring Boot's autoconfigured
 * {@code ObjectMapper} auto-detects and installs any {@code com.fasterxml.jackson.databind.Module} bean.
 *
 * <p>Assumes {@link LinkPlatform} follows the same {@code getWireValue()} /
 * {@code fromWireValue(String)} pattern already implemented on {@link TicketStatus} and
 * {@link Priority} (verified by reading those two files) — A1 owns {@code LinkPlatform} and has
 * not landed it yet at the time this file was written.
 */
public class DomainEnumModule extends SimpleModule {

    public DomainEnumModule() {
        super("DomainEnumModule");

        addSerializer(TicketStatus.class, new JsonSerializer<TicketStatus>() {
            @Override
            public void serialize(TicketStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.getWireValue());
            }
        });
        addDeserializer(TicketStatus.class, new JsonDeserializer<TicketStatus>() {
            @Override
            public TicketStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return TicketStatus.fromWireValue(p.getValueAsString());
            }
        });

        addSerializer(Priority.class, new JsonSerializer<Priority>() {
            @Override
            public void serialize(Priority value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.getWireValue());
            }
        });
        addDeserializer(Priority.class, new JsonDeserializer<Priority>() {
            @Override
            public Priority deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return Priority.fromWireValue(p.getValueAsString());
            }
        });

        addSerializer(LinkPlatform.class, new JsonSerializer<LinkPlatform>() {
            @Override
            public void serialize(LinkPlatform value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.getWireValue());
            }
        });
        addDeserializer(LinkPlatform.class, new JsonDeserializer<LinkPlatform>() {
            @Override
            public LinkPlatform deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return LinkPlatform.fromWireValue(p.getValueAsString());
            }
        });
    }
}
