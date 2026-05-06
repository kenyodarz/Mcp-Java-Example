package co.com.bancolombia.mcp.tools;

import co.com.bancolombia.usecase.SaludoUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("SaludoTool Unit Tests")
class SaludoToolTest {

    // ==================== TEST DOUBLES & SUT ====================
    // Sistema bajo prueba (SUT): La herramienta MCP para saludos personalizados
    private final SaludoTool saludoToolSUT = new SaludoTool(new SaludoUseCase());

    @Test
    @DisplayName("Debe retornar un saludo personalizado cuando el nombre es válido")
    void shouldReturnPersonalizedGreeting() {
        // ==================== GIVEN ====================
        // Preparar un nombre válido para personalizar el saludo
        String validPersonName = "Jorge";

        // ==================== WHEN ====================
        // Ejecutar la herramienta con el nombre válido
        var salutoToolResult = saludoToolSUT.saludo(validPersonName);

        // ==================== THEN ====================
        // Verificar que retorna un saludo personalizado
        StepVerifier.create(salutoToolResult)
                .expectNext("¡Hola Jorge! Bienvenido al servidor MCP de Bancolombia. " +
                        "¿En qué puedo ayudarte hoy?")
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar mensaje genérico cuando el nombre es vacío o nulo")
    void shouldReturnGenericGreetingWhenNameInvalid() {
        // ==================== GIVEN ====================
        // Preparar un nombre inválido (espacios en blanco)
        String invalidNameWithOnlyWhitespace = "   ";

        // ==================== WHEN ====================
        // Ejecutar la herramienta con el nombre inválido
        var salutoToolResult = saludoToolSUT.saludo(invalidNameWithOnlyWhitespace);

        // ==================== THEN ====================
        // Verificar que retorna un saludo genérico
        StepVerifier.create(salutoToolResult)
                .expectNext("¡Hola! ¿Cómo te llamas?")
                .verifyComplete();
    }
}
