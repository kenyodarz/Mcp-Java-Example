package co.com.bancolombia.usecase.apikey;

import co.com.bancolombia.model.apikey.ApiKey;
import co.com.bancolombia.model.apikey.gateways.ApiKeyGateway;
import co.com.bancolombia.model.apikey.gateways.PasswordEncoderGateway;
import java.util.logging.Level;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para validar una API Key
 * <p>
 * Verifica que: 1. La API Key exista 2. El secret sea correcto 3. Esté activa 4. No haya expirado
 * <p>
 * Este caso de uso NO depende de frameworks (Spring, BCrypt, etc.) sino de abstracciones definidas
 * en el dominio (gateways).
 */
@Log
public record ValidateApiKeyUseCase(
        ApiKeyGateway apiKeyGateway,
        PasswordEncoderGateway passwordEncoderGateway
) {

    /**
     * Valida una API Key con formato: "id.secret"
     *
     * @param apiKeyString String con formato "id.secret"
     * @return Mono con la ApiKey si es válida, Mono.empty() si no
     */
    public Mono<ApiKey> execute(String apiKeyString) {
        log.log(Level.INFO, "🔍 Validando API Key");

        return parseApiKeyString(apiKeyString)
                .flatMap(parts -> validateApiKey(parts[0], parts[1]))
                .doOnSuccess(key -> {
                    if (key != null) {
                        log.log(Level.INFO, "✅ API Key válida: {}", key.getId());
                        // Actualizar último uso de forma asíncrona (fire and forget)
                        apiKeyGateway.updateLastUsed(key.getId()).subscribe();
                    }
                })
                .doOnError(error -> log.log(Level.SEVERE, "❌ Error validando API Key", error));
    }

    /**
     * Parsea el string "id.secret" en sus componentes
     */
    private Mono<String[]> parseApiKeyString(String apiKeyString) {
        return Mono.fromCallable(() -> {
            if (apiKeyString == null || apiKeyString.trim().isEmpty()) {
                throw new IllegalArgumentException("API Key vacía");
            }

            String[] parts = apiKeyString.split("\\.", 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Formato de API Key inválido. Debe ser: id.secret");
            }

            return parts;
        });
    }

    /**
     * Valida el ID y secret contra la base de datos
     */
    private Mono<ApiKey> validateApiKey(String id, String secret) {
        return apiKeyGateway.findById(id)
                .flatMap(apiKey -> {
                    // Verificar que esté activa y no expirada
                    if (!apiKey.isValid()) {
                        log.log(Level.WARNING, "⚠️ API Key inactiva o expirada: {}", id);
                        return Mono.empty();
                    }

                    // Verificar el secret
                    if (!passwordEncoderGateway.matches(secret, apiKey.getSecretHash())) {
                        log.log(Level.WARNING, "⚠️ Secret incorrecto para API Key: {}", id);
                        return Mono.empty();
                    }

                    return Mono.just(apiKey);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.log(Level.WARNING, "⚠️ API Key no encontrada: {}", id);
                    return Mono.empty();
                }));
    }
}