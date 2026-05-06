package co.com.bancolombia.mcp.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("HealthTool Unit Tests")
class HealthToolTest {

    @Test
    @DisplayName("healthCheck debe retornar 'OK'")
    void healthCheckShouldReturnOK() {
        // ==================== GIVEN ====================
        // Preparar la herramienta MCP de health check
        HealthTool healthToolSUT = new HealthTool();

        // ==================== WHEN ====================
        // Ejecutar el health check de la herramienta
        var healthCheckResult = healthToolSUT.healthCheck();

        // ==================== THEN ====================
        // Verificar que el servidor está saludable retornando 'OK'
        StepVerifier.create(healthCheckResult)
                .expectNext("OK")
                .verifyComplete();
    }
}
