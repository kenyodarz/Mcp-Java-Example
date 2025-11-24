package co.com.bancolombia.r2dbc.apikey;

import co.com.bancolombia.model.apikey.ApiKey;
import co.com.bancolombia.model.apikey.gateways.ApiKeyGateway;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador que implementa el gateway de API Keys usando R2DBC
 * <p>
 * Esta clase traduce entre el modelo de dominio (ApiKey) y el de persistencia (ApiKeyEntity)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ApiKeyRepositoryAdapter implements ApiKeyGateway {

    private final ApiKeyR2dbcRepository repository;

    @Override
    public Mono<ApiKey> findById(String id) {
        log.debug("🔍 Buscando API Key por ID: {}", id);
        return repository.findById(id)
                .map(this::toDomain)
                .doOnSuccess(key -> {
                    if (key != null) {
                        log.debug("✅ API Key encontrada: {}", id);
                    } else {
                        log.debug("⚠️ API Key no encontrada: {}", id);
                    }
                });
    }

    @Override
    public Mono<ApiKey> findBySecretHash(String secretHash) {
        return repository.findBySecretHash(secretHash)
                .map(this::toDomain);
    }

    @Override
    public Mono<ApiKey> save(ApiKey apiKey) {
        log.debug("💾 Guardando API Key: {}", apiKey.getId());

        ApiKeyEntity entity = toEntity(apiKey);

        return repository.save(entity)
                .map(this::toDomain)
                .doOnSuccess(saved -> log.info("✅ API Key guardada: {}", saved.getId()));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        log.info("🗑️ Eliminando API Key: {}", id);
        return repository.deleteById(id);
    }

    @Override
    public Flux<ApiKey> findAll() {
        return repository.findAll()
                .map(this::toDomain);
    }

    @Override
    public Flux<ApiKey> findAllActive() {
        return repository.findByEnabledTrue()
                .map(this::toDomain);
    }

    @Override
    public Flux<ApiKey> findExpiringSoon(int daysBeforeExpiration) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(daysBeforeExpiration);
        return repository.findExpiringSoonAndEnabled(expirationDate)
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> updateLastUsed(String id) {
        return repository.findById(id)
                .flatMap(entity -> {
                    entity.setLastUsedAt(LocalDateTime.now());
                    entity.setUsageCount(
                            entity.getUsageCount() != null ? entity.getUsageCount() + 1 : 1L
                    );
                    return repository.save(entity);
                })
                .then();
    }

    // En ApiKeyRepositoryAdapter.java

    @Override
    public Flux<ApiKey> findAllEnabled() {
        return repository.findByEnabledTrue()
                .map(this::toDomain);
    }

    // ========================================
    // Mappers: Domain ↔ Entity
    // ========================================

    /**
     * Convierte de Entity (BD) a Domain
     */
    private ApiKey toDomain(ApiKeyEntity entity) {
        return ApiKey.builder()
                .id(entity.getId())
                .name(entity.getName())
                .secretHash(entity.getSecretHash())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .expiresAt(entity.getExpiresAt())
                .lastUsedAt(entity.getLastUsedAt())
                .usageCount(entity.getUsageCount())
                .description(entity.getDescription())
                .allowedIp(entity.getAllowedIp())
                .build();
    }

    /**
     * Convierte de Domain a Entity (BD)
     */
    private ApiKeyEntity toEntity(ApiKey domain) {
        return ApiKeyEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .secretHash(domain.getSecretHash())
                .enabled(domain.getEnabled())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .expiresAt(domain.getExpiresAt())
                .lastUsedAt(domain.getLastUsedAt())
                .usageCount(domain.getUsageCount())
                .description(domain.getDescription())
                .allowedIp(domain.getAllowedIp())
                .build();
    }
}