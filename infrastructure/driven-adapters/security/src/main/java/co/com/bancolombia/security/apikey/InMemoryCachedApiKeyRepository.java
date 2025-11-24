package co.com.bancolombia.security.apikey;

import co.com.bancolombia.model.apikey.ApiKey;
import co.com.bancolombia.model.apikey.gateways.ApiKeyGateway;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryCachedApiKeyRepository {

    private final ApiKeyGateway apiKeyGateway;
    private final LoadingCache<String, SimpleApiKeyEntity> cache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofHours(12))
            .refreshAfterWrite(Duration.ofMinutes(30))
            .recordStats()
            .build(this::loadApiKey);

    @PostConstruct
    public void warmUpCache() {
        log.info("Iniciando warm-up del caché de API Keys...");
        apiKeyGateway.findAllEnabled()
                .map(this::toSimpleEntity)
                .doOnNext(entity -> {
                    if (entity != null) {
                        cache.put(entity.id(), entity);
                    }
                })
                .doOnComplete(() -> log.info("Caché de API Keys cargado con {} claves",
                        cache.asMap().size()))
                .subscribe();
    }

    public Mono<SimpleApiKeyEntity> findByKeyId(String keyId) {
        return Mono.fromCallable(() -> cache.get(keyId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private SimpleApiKeyEntity loadApiKey(String keyId) {
        log.debug("Loading API Key from DB (miss/refresh): {}", keyId);
        ApiKey domainKey = apiKeyGateway.findById(keyId)
                .block(Duration.ofSeconds(2));
        return toSimpleEntity(domainKey);
    }

    private SimpleApiKeyEntity toSimpleEntity(ApiKey domain) {
        if (domain == null || !Boolean.TRUE.equals(domain.getEnabled())) {
            return null;
        }
        return new SimpleApiKeyEntity(domain.getId(), domain.getSecretHash(), true);
    }

    public CacheMetrics getCacheMetrics() {
        var stats = cache.stats();
        return new CacheMetrics(
                cache.asMap().size(),
                stats.hitRate(),
                stats.hitCount(),
                stats.missCount(),
                stats.evictionCount(),
                stats.loadSuccessCount()
        );
    }

    public record SimpleApiKeyEntity(String id, String secretHash, boolean enabled) {

    }

    public record CacheMetrics(long size, double hitRate, long hitCount, long missCount,
                               long evictionCount, long loadSuccessCount) {

    }
}