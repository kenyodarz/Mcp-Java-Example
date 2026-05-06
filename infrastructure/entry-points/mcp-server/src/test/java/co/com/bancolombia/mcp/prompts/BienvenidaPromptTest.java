package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class BienvenidaPromptTest {

    // ==================== TEST DOUBLES & SUT ====================
    // Sistema bajo prueba (SUT): El prompt MCP para mensajes de bienvenida
    private final BienvenidaPrompt bienvenidaPromptSUT = new BienvenidaPrompt();

    @Test
    @DisplayName("Debe generar saludo sin título cuando es null")
    void shouldGenerateWelcomeWithoutTitleWhenNull() {
        // ==================== GIVEN ====================
        // Preparar un título nulo para verificar saludo genérico

        // ==================== WHEN ====================
        // Ejecutar el prompt de bienvenida sin título

        // ==================== THEN ====================
        // Verificar que genera saludo genérico sin incluir un título
        StepVerifier.create(bienvenidaPromptSUT.getBienvenidaPrompt(null))
                .assertNext(promptResult -> {
                    assert promptResult.description().equals("Bienvenida formal");

                    PromptMessage promptMessage = promptResult.messages().getFirst();
                    TextContent promptContent = (TextContent) promptMessage.content();

                    assert promptMessage.role() == Role.USER;
                    assert promptContent.text().equals(
                            "Bienvenido/a. Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?"
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe generar saludo sin título cuando está vacío")
    void shouldGenerateWelcomeWithoutTitleWhenEmpty() {
        // ==================== GIVEN ====================
        // Preparar un título vacío para verificar saludo genérico

        // ==================== WHEN ====================
        // Ejecutar el prompt de bienvenida con título vacío

        // ==================== THEN ====================
        // Verificar que genera saludo genérico sin incluir un título
        StepVerifier.create(bienvenidaPromptSUT.getBienvenidaPrompt(""))
                .assertNext(promptResult -> {
                    PromptMessage promptMessage = promptResult.messages().getFirst();
                    TextContent promptContent = (TextContent) promptMessage.content();

                    assert promptContent.text().equals(
                            "Bienvenido/a. Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?"
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe generar saludo con título cuando es enviado")
    void shouldGenerateWelcomeWithTitle() {
        // ==================== GIVEN ====================
        // Preparar un título personalizado para el saludo
        String personalizedGreetingTitle = "Dr. House";

        // ==================== WHEN ====================
        // Ejecutar el prompt de bienvenida con título personalizado

        // ==================== THEN ====================
        // Verificar que genera saludo personalizado incluyendo el título
        StepVerifier.create(bienvenidaPromptSUT.getBienvenidaPrompt(personalizedGreetingTitle))
                .assertNext(promptResult -> {
                    PromptMessage promptMessage = promptResult.messages().getFirst();
                    TextContent promptContent = (TextContent) promptMessage.content();

                    assert promptContent.text().equals(
                            "Bienvenido/a, Dr. House. Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?"
                    );
                })
                .verifyComplete();
    }
}
