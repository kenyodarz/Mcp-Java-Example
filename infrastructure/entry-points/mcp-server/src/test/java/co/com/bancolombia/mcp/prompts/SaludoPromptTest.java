package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class SaludoPromptTest {

    private final SaludoPrompt prompt = new SaludoPrompt();

    @Test
    @DisplayName("Debe generar un prompt de saludo correctamente")
    void shouldGenerateSaludoPrompt() {
        // Given
        String nombre = "Jorge";

        // When
        var resultMono = prompt.getSaludoPrompt(nombre);

        // Then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    // Verifica contenido
                    assert result.messages().size() == 1;

                    PromptMessage message = result.messages().getFirst();
                    assert message.role() == Role.USER;

                    TextContent content = (TextContent) message.content();
                    assert content.text().equals("Hola Jorge, ¿en qué te ayudo?");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe fallar si el nombre es null (validación de anotación @McpArg)")
    void shouldFailWhenNameIsNull() {
        StepVerifier.create(prompt.getSaludoPrompt(null))
                .expectError() // Spring AI MCP valida @McpArg(required=true)
                .verify();
    }
}
