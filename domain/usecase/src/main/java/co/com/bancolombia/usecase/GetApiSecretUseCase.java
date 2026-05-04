package co.com.bancolombia.usecase;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * UseCase para obtener secretos de APIs externas de forma reactiva.
 * <p>
 * Delega a AsyncSecretsGateway (que implementa SecretsManagerAdapter) para obtener keys/credentials
 * desde ministack (dev) o AWS (prod).
 */
@RequiredArgsConstructor
public class GetApiSecretUseCase {

    private final AsyncSecretsGateway secretsGateway;

    /**
     * Obtiene un secret de API externa por nombre.
     *
     * @param secretName nombre del secret (ej: "api-consumer-key")
     * @return Mono<String> con el valor del secret
     */
    public Mono<String> execute(String secretName) {
        return secretsGateway.getSecret(secretName);
    }
}

