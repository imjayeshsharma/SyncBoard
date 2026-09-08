// Stage 6 — Cache + realtime
package com.syncboard.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Stage 6 cache wiring.
 *
 * <p><b>Profiles:</b> the {@code local} profile sets {@code spring.cache.type=simple} in
 * {@code application.yml}, so Boot skips Redis cache auto-configuration entirely in favour
 * of an in-memory {@code ConcurrentMapCacheManager}. That path is a no-op fallback with no
 * network calls, so it cannot fail the way Redis can and needs no error handling — this
 * class carries no {@code @Profile} restriction of its own; {@link #errorHandler()} is
 * simply irrelevant, not disabled, when {@code simple} is active.
 *
 * <p><b>PRD "Cache Unavailable":</b> when {@code spring.cache.type=redis} and Redis is
 * unreachable, board reads must degrade to PostgreSQL rather than fail the request.
 * {@link #errorHandler()} logs and swallows every GET/PUT/EVICT/CLEAR failure so the
 * {@code @Cacheable}/{@code @CacheEvict}-annotated service method body always runs.
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /** Cache name constants — avoids stringly-typed {@code @Cacheable("board")} literals. */
    public static final class CacheNames {
        public static final String BOARD = "board";

        private CacheNames() {
        }
    }

    private final ObjectProvider<CacheManager> cacheManagerProvider;

    public CacheConfig(ObjectProvider<CacheManager> cacheManagerProvider) {
        this.cacheManagerProvider = cacheManagerProvider;
    }

    /**
     * Customises Boot's auto-configured {@code RedisCacheManager} (active whenever
     * {@code spring.cache.type=redis}): TTL from
     * {@code syncboard.board.cache-ttl-seconds} and JSON value serialization so cached
     * {@code BoardResponse} payloads are human-readable outside the JVM and portable
     * across app restarts/redeploys.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(SyncBoardProperties properties) {
        RedisCacheConfiguration boardCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(properties.board().cacheTtlSeconds()))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return builder -> builder.withCacheConfiguration(CacheNames.BOARD, boardCacheConfig);
    }

    /**
     * Registering this {@link CachingConfigurer} must not short-circuit Boot's
     * profile-driven choice of {@code CacheManager} (Redis vs. the {@code local}
     * profile's simple in-memory one) — so this simply delegates to whichever one Boot
     * already auto-configured, rather than constructing one itself.
     */
    @Override
    public CacheManager cacheManager() {
        return cacheManagerProvider.getIfAvailable();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new GracefulCacheErrorHandler();
    }

    /** Logs and swallows cache failures so callers always fall through to PostgreSQL. */
    private static final class GracefulCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Redis cache GET failed on cache '{}' key '{}' — falling through to PostgreSQL: {}",
                    cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("Redis cache PUT failed on cache '{}' key '{}' — value not cached: {}",
                    cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Redis cache EVICT failed on cache '{}' key '{}': {}",
                    cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Redis cache CLEAR failed on cache '{}': {}", cache.getName(), exception.getMessage());
        }
    }
}
