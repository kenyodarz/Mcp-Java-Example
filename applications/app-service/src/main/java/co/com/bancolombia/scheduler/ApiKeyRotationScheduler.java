package co.com.bancolombia.scheduler;

import co.com.bancolombia.usecase.apikey.RotateExpiredApiKeysUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para rotar API Keys expiradas automáticamente
 * <p>
 * Este componente ejecuta tareas programadas para: 1. Desactivar API Keys expiradas 2. Notificar
 * API Keys próximas a expirar
 * <p>
 * Se puede habilitar/deshabilitar desde application.yaml
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "security.apikey.rotation.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ApiKeyRotationScheduler {

    private final RotateExpiredApiKeysUseCase rotateExpiredApiKeysUseCase;

    /**
     * Rota API Keys expiradas cada día a las 2:00 AM
     * <p>
     * Cron: segundos minutos horas día mes día-semana "0 0 2 * * *" = Cada día a las 02:00:00
     */
    @Scheduled(cron = "${security.apikey.rotation.cron:0 0 2 * * *}")
    public void rotateExpiredApiKeys() {
        log.info("🔄 Iniciando tarea programada: Rotación de API Keys expiradas");

        rotateExpiredApiKeysUseCase.execute()
                .collectList()
                .subscribe(
                        rotatedKeys -> {
                            if (rotatedKeys.isEmpty()) {
                                log.info("✅ No hay API Keys expiradas para rotar");
                            } else {
                                log.info("✅ API Keys rotadas: {}", rotatedKeys.size());
                                rotatedKeys.forEach(key ->
                                        log.info("   - {}: {}", key.getId(), key.getName())
                                );
                            }
                        },
                        error -> log.error("❌ Error en rotación de API Keys", error)
                );
    }

    /**
     * Notifica API Keys próximas a expirar (30 días) Se ejecuta cada lunes a las 9:00 AM
     * <p>
     * Cron: "0 0 9 * * MON"
     */
    @Scheduled(cron = "${security.apikey.notification.cron:0 0 9 * * MON}")
    public void notifyExpiringSoonApiKeys() {
        log.info("📧 Iniciando tarea programada: Notificación de API Keys próximas a expirar");

        int daysBeforeExpiration = 30;

        rotateExpiredApiKeysUseCase.notifyExpiringSoon(daysBeforeExpiration)
                .collectList()
                .subscribe(
                        expiringKeys -> {
                            if (expiringKeys.isEmpty()) {
                                log.info(
                                        "✅ No hay API Keys próximas a expirar en los próximos {} días",
                                        daysBeforeExpiration);
                            } else {
                                log.warn("⚠️ API Keys próximas a expirar: {}", expiringKeys.size());
                                expiringKeys.forEach(key ->
                                        log.warn("   - {}: {} (expira: {})",
                                                key.getId(),
                                                key.getName(),
                                                key.getExpiresAt())
                                );
                            }
                        },
                        error -> log.error("❌ Error notificando API Keys próximas a expirar", error)
                );
    }
}