package co.com.bancolombia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para habilitar tareas programadas (Scheduling)
 * <p>
 * Esta anotación habilita el procesamiento de @Scheduled en: - ApiKeyRotationScheduler -
 * InMemoryCachedApiKeyRepository (refresh de caché)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

}
