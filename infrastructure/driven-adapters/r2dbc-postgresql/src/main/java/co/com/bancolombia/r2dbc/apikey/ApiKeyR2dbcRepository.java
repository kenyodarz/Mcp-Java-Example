package co.com.bancolombia.r2dbc.apikey;

import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApiKeyR2dbcRepository extends R2dbcRepository<ApiKeyEntity, String> {

    Mono<ApiKeyEntity> findBySecretHash(String secretHash);

    Flux<ApiKeyEntity> findByEnabledTrue();

    Flux<ApiKeyEntity> findByExpiresAtBeforeAndEnabledIsTrue(LocalDateTime date);

}
