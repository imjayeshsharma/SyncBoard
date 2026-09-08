// Stage 7 — GraphQL read API
package com.syncboard.graphql;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Registers the schema-first {@code DateTime} scalar declared in
 * {@code graphql/schema.graphqls}, mapping it to {@link Instant} and (de)serialising as
 * ISO-8601 UTC (e.g. {@code 2026-09-08T12:34:56Z}) — the same format the REST API uses for
 * timestamps, so clients handle both transports identically.
 */
@Configuration
public class GraphQlScalarConfig {

    private static final Coercing<Instant, String> DATE_TIME_COERCING = new Coercing<>() {

        @Override
        public String serialize(Object dataFetcherResult, GraphQLContext graphQlContext, Locale locale) {
            if (dataFetcherResult instanceof Instant instant) {
                return instant.toString();
            }
            throw new CoercingSerializeException("Expected an Instant but was " + dataFetcherResult);
        }

        @Override
        public Instant parseValue(Object input, GraphQLContext graphQlContext, Locale locale) {
            try {
                return Instant.parse(input.toString());
            } catch (DateTimeParseException e) {
                throw new CoercingParseValueException("Not a valid ISO-8601 instant: " + input, e);
            }
        }

        @Override
        public Instant parseLiteral(Value<?> input, CoercedVariables variables, GraphQLContext graphQlContext,
                Locale locale) {
            if (input instanceof StringValue stringValue) {
                try {
                    return Instant.parse(stringValue.getValue());
                } catch (DateTimeParseException e) {
                    throw new CoercingParseLiteralException(
                            "Not a valid ISO-8601 instant: " + stringValue.getValue(), e);
                }
            }
            throw new CoercingParseLiteralException("Expected a StringValue but was " + input);
        }
    };

    public static final GraphQLScalarType DATE_TIME_SCALAR = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("An ISO-8601 UTC instant, e.g. 2026-09-08T12:34:56Z")
            .coercing(DATE_TIME_COERCING)
            .build();

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(DATE_TIME_SCALAR);
    }
}
