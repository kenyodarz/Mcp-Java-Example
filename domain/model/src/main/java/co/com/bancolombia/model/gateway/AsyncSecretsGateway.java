package co.com.bancolombia.model.gateway;

import reactor.core.publisher.Mono;

/**
 * Gateway para obtener secretos de forma reactiva. Abstrae ministack (dev) y AWS Secrets Manager
 * (prod).
 */
public interface AsyncSecretsGateway {

    /**
     * Obtiene un secret por nombre de forma reactiva.
     *
     * @param secretName nombre del secret a obtener
     * @return Mono<String> con el valor del secret
     */
    Mono<String> getSecret(String secretName);
}

