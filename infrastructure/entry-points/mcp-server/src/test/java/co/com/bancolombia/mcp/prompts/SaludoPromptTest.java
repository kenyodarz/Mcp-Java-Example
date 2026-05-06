package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class SaludoPromptTest {

    // ==================== TEST DOUBLES & SUT ====================
    // Sistema bajo prueba (SUT): El prompt MCP para saludos personalizados
    private final SaludoPrompt saludoPromptSUT = new SaludoPrompt();

    @Test
    @DisplayName("Debe generar un prompt de saludo correctamente")
    void shouldGenerateSaludoPrompt() {
        // ==================== GIVEN ====================
        // Preparar el nombre para personalizar el prompt de saludo
        String personNameForSaludo = "Jorge";

        // ==================== WHEN ====================
        // Ejecutar la generación del prompt de saludo personalizado
        var saludoPromptResultMono = saludoPromptSUT.getSaludoPrompt(personNameForSaludo);

        // ==================== THEN ====================
        // Verificar que el prompt se construye correctamente con el nombre
        StepVerifier.create(saludoPromptResultMono)
                .assertNext(promptResult -> {
                    // Verificar que hay exactamente un mensaje
                    assert promptResult.messages().size() == 1;

                    PromptMessage promptMessage = promptResult.messages().getFirst();
                    assert promptMessage.role() == Role.USER;

                    TextContent promptContent = (TextContent) promptMessage.content();
                    assert promptContent.text().equals("Hola Jorge, ¿en qué te ayudo?");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe fallar si el nombre es null (validación de anotación @McpArg)")
    void shouldFailWhenNameIsNull() {
        // ==================== GIVEN ====================
        // Preparar un nombre nulo para probar validación

        // ==================== WHEN ====================
        // Ejecutar el prompt con nombre nulo

        // ==================== THEN ====================
        // Verificar que falla debido a validación de @McpArg(required=true)
        StepVerifier.create(saludoPromptSUT.getSaludoPrompt(null))
                .expectError() // Spring AI MCP valida @McpArg(required=true)
                .verify();
    }
}
