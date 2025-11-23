package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class BienvenidaPromptTest {

    private final BienvenidaPrompt prompt = new BienvenidaPrompt();

    @Test
    @DisplayName("Debe generar saludo sin título cuando es null")
    void shouldGenerateWelcomeWithoutTitleWhenNull() {
        StepVerifier.create(prompt.getBienvenidaPrompt(null))
                .assertNext(result -> {
                    assert result.description().equals("Bienvenida formal");

                    PromptMessage message = result.messages().getFirst();
                    TextContent content = (TextContent) message.content();

                    assert message.role() == Role.USER;
                    assert content.text().equals(
                            "Bienvenido/a. Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?"
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe generar saludo sin título cuando está vacío")
    void shouldGenerateWelcomeWithoutTitleWhenEmpty() {
        StepVerifier.create(prompt.getBienvenidaPrompt(""))
                .assertNext(result -> {
                    PromptMessage message = result.messages().getFirst();
                    TextContent content = (TextContent) message.content();

                    assert content.text().equals(
                            "Bienvenido/a. Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?"
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe generar saludo con título cuando es enviado")
    void shouldGenerateWelcomeWithTitle() {
        StepVerifier.create(prompt.getBienvenidaPrompt("Dr. House"))
                .assertNext(result -> {
                    PromptMessage message = result.messages().getFirst();
                    TextContent content = (TextContent) message.content();

                    assert content.text().equals(
                            "Bienvenido/a, Dr. House. Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?"
                    );
                })
                .verifyComplete();
    }
}
