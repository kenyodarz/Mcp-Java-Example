package co.com.bancolombia.usecase.apikey;

import co.com.bancolombia.model.apikey.ApiKey;
import co.com.bancolombia.model.apikey.gateways.ApiKeyGateway;
import java.time.LocalDateTime;
import java.util.logging.Level;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para rotar API Keys expiradas
 * <p>
 * Este caso de uso: 1. Busca API Keys expiradas 2. Las desactiva 3. Genera un reporte de keys
 * rotadas
 */
@Log
public record RotateExpiredApiKeysUseCase(ApiKeyGateway apiKeyGateway) {

    /**
     * Ejecuta la rotación de API Keys expiradas
     *
     * @return Flux con las API Keys que fueron desactivadas
     */
    public Flux<ApiKey> execute() {
        log.info("🔄 Iniciando rotación de API Keys expiradas");

        LocalDateTime now = LocalDateTime.now();

        return apiKeyGateway.findAll()
                .filter(apiKey -> isExpired(apiKey, now))
                .flatMap(this::deactivateApiKey)
                .doOnComplete(() -> log.info("✅ Rotación de API Keys completada"))
                .doOnError(
                        error -> log.log(Level.SEVERE, "❌ Error en rotación de API Keys", error));
    }

    /**
     * Verifica si una API Key está expirada
     */
    private boolean isExpired(ApiKey apiKey, LocalDateTime now) {
        return apiKey.getExpiresAt() != null
                && now.isAfter(apiKey.getExpiresAt())
                && Boolean.TRUE.equals(apiKey.getEnabled());
    }

    /**
     * Desactiva una API Key expirada
     */
    private Mono<ApiKey> deactivateApiKey(ApiKey apiKey) {
        log.info(String.format("⚠️ Desactivando API Key expirada: %s (expiró el: %s)",
                apiKey.getId(),
                apiKey.getExpiresAt()));

        ApiKey deactivated = apiKey.toBuilder()
                .enabled(false)
                .updatedAt(LocalDateTime.now())
                .build();

        return apiKeyGateway.save(deactivated);
    }

    /**
     * Ejecuta una notificación de API Keys próximas a expirar
     *
     * @param daysBeforeExpiration Días antes de la expiración
     * @return Flux con las API Keys próximas a expirar
     */
    public Flux<ApiKey> notifyExpiringSoon(int daysBeforeExpiration) {
        log.info(String.format("📧 Verificando API Keys próximas a expirar (en %s días)",
                daysBeforeExpiration));

        return apiKeyGateway.findExpiringSoon(daysBeforeExpiration)
                .doOnNext(apiKey ->
                        log.log(Level.WARNING,
                                String.format("⚠️ API Key próxima a expirar: %s (expira el: %s)",
                                        apiKey.getId(),
                                        apiKey.getExpiresAt()))
                );
    }
}