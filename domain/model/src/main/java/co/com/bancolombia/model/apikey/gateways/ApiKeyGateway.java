package co.com.bancolombia.model.apikey.gateways;

import co.com.bancolombia.model.apikey.ApiKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Gateway (Puerto de salida) para gestionar API Keys
 *
 * Esta interfaz define las operaciones de persistencia para API Keys
 * sin exponer detalles de implementación (JPA, R2DBC, MongoDB, etc.)
 */
public interface ApiKeyGateway {

    /**
     * Busca una API Key por su ID
     *
     * @param id ID de la API Key
     * @return Mono con la API Key si existe, Mono.empty() si no
     */
    Mono<ApiKey> findById(String id);

    /**
     * Busca una API Key por el hash del secret
     *
     * @param secretHash Hash del secret
     * @return Mono con la API Key si existe, Mono.empty() si no
     */
    Mono<ApiKey> findBySecretHash(String secretHash);

    /**
     * Guarda o actualiza una API Key
     *
     * @param apiKey API Key a guardar
     * @return Mono con la API Key guardada
     */
    Mono<ApiKey> save(ApiKey apiKey);

    /**
     * Elimina una API Key por su ID
     *
     * @param id ID de la API Key
     * @return Mono<Void> cuando se complete la eliminación
     */
    Mono<Void> deleteById(String id);

    /**
     * Lista todas las API Keys
     *
     * @return Flux con todas las API Keys
     */
    Flux<ApiKey> findAll();

    /**
     * Lista solo las API Keys activas y no expiradas
     *
     * @return Flux con las API Keys activas
     */
    Flux<ApiKey> findAllEnabled();

    /**
     * Lista las API Keys que están por expirar
     *
     * @param daysBeforeExpiration Días antes de la expiración
     * @return Flux con las API Keys próximas a expirar
     */
    Flux<ApiKey> findExpiringSoon(int daysBeforeExpiration);

    /**
     * Actualiza el último uso de una API Key
     *
     * @param id ID de la API Key
     * @return Mono<Void> cuando se complete la actualización
     */
    Mono<Void> updateLastUsed(String id);
}