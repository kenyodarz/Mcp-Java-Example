package co.com.bancolombia.mcp.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("HealthTool Unit Tests")
class HealthToolTest {

    @Test
    @DisplayName("healthCheck debe retornar 'OK'")
    void healthCheckShouldReturnOK() {

        // Arrange
        HealthTool tool = new HealthTool();

        // Act
        var result = tool.healthCheck();

        // Assert
        StepVerifier.create(result)
                .expectNext("OK")
                .verifyComplete();
    }
}
