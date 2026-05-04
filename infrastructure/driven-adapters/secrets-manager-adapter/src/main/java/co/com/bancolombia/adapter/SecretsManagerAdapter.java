package co.com.bancolombia.adapter;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Adapter reactivo para obtener secretos desde AWS Secrets Manager o ministack. Implementa
 * AsyncSecretsGateway.
 */
@Slf4j
@RequiredArgsConstructor
public class SecretsManagerAdapter implements AsyncSecretsGateway {

    private final SecretsManagerAsyncClient secretsManagerAsyncClient;

    @Override
    public Mono<String> getSecret(String secretName) {
        log.debug("Obteniendo secret: {}", secretName);

        return Mono.fromFuture(
                        secretsManagerAsyncClient.getSecretValue(
                                GetSecretValueRequest.builder()
                                        .secretId(secretName)
                                        .build()
                        )
                )
                .map(GetSecretValueResponse::secretString)
                .doOnError(error -> log.error("Error obteniendo secret {}: {}", secretName,
                        error.getMessage()))
                .onErrorMap(
                        error -> new RuntimeException("No se pudo obtener el secret: " + secretName,
                                error));
    }
}

