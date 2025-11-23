package co.com.bancolombia.mcp.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("SaludoTool Unit Tests")
class SaludoToolTest {

    private final SaludoTool tool = new SaludoTool();

    @Test
    @DisplayName("Debe retornar un saludo personalizado cuando el nombre es válido")
    void shouldReturnPersonalizedGreeting() {

        // Arrange
        String name = "Jorge";

        // Act
        var result = tool.saludo(name);

        // Assert
        StepVerifier.create(result)
                .expectNext("¡Hola Jorge! Bienvenido al servidor MCP de Bancolombia. " +
                        "¿En qué puedo ayudarte hoy?")
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar mensaje genérico cuando el nombre es vacío o nulo")
    void shouldReturnGenericGreetingWhenNameInvalid() {

        // Arrange
        String name = "   "; // vacío

        // Act
        var result = tool.saludo(name);

        // Assert
        StepVerifier.create(result)
                .expectNext("¡Hola! ¿Cómo te llamas?")
                .verifyComplete();
    }
}
