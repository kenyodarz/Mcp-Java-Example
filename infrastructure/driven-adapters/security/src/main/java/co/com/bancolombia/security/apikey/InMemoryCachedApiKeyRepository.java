// infrastructure/driven-adapters/security/src/main/java/co/com/bancolombia/security/apikey/InMemoryCachedApiKeyRepository.java

package co.com.bancolombia.security.apikey;

import co.com.bancolombia.model.apikey.ApiKey;
import co.com.bancolombia.model.apikey.gateways.ApiKeyGateway;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntity;
import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryCachedApiKeyRepository implements ApiKeyEntityRepository<ApiKeyEntity> {

    private final ApiKeyGateway apiKeyGateway;

    private final Cache<String, ApiKeyEntity> cache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofHours(12))
            .refreshAfterWrite(Duration.ofMinutes(30))
            .recordStats()
            .build();

    @PostConstruct
    public void warmUpCache() {
        log.info("Iniciando warm-up del caché de API Keys...");
        apiKeyGateway.findAllEnabled()
                .map(this::toMcpEntity)
                .doOnNext(entity -> cache.put(entity.getId(), entity))
                .doOnComplete(() -> log.info("Caché de API Keys cargado con {} claves",
                        cache.asMap().size()))
                .doOnError(e -> log.error("Error en warm-up del caché de API Keys", e))
                .subscribe();
    }

    @Override
    public ApiKeyEntity findByKeyId(String keyId) {
        return cache.get(keyId, id -> {
            log.debug("Cache MISS para API Key: {}", id);
            ApiKey domainKey = apiKeyGateway.findById(id)
                    .block(Duration.ofMillis(500));
            return toMcpEntity(domainKey);
        });
    }

    private ApiKeyEntity toMcpEntity(co.com.bancolombia.model.apikey.ApiKey domain) {
        if (domain == null || !Boolean.TRUE.equals(domain.getEnabled())) {
            return null;
        }

        return new ApiKeyEntity() {
            @Override
            public String getId() {
                return domain.getId();
            }

            @Override
            public String getSecret() {
                return domain.getSecretHash();
            }

            public boolean isEnabled() {
                return true;
            }

            @Override
            public List<org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public void eraseCredentials() {
            }

            @Override
            public ApiKeyEntity copy() {
                return this;
            }
        };
    }
}