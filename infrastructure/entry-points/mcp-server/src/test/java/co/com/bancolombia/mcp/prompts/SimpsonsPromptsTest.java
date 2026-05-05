package co.com.bancolombia.mcp.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class SimpsonsPromptsTest {

    private final SimpsonsPrompts prompts = new SimpsonsPrompts();

    @Test
    void shouldBuildCharacterProfilePrompt() {
        StepVerifier.create(prompts.characterProfile("Homer Simpson", "Enfatiza su familia"))
                .assertNext(result -> {
                    assertEquals("Perfil de personaje", result.description());
                    PromptMessage message = result.messages().getFirst();
                    assertEquals(Role.USER, message.role());
                    assertTrue(((TextContent) message.content()).text().contains("Homer Simpson"));
                    assertTrue(((TextContent) message.content()).text()
                            .contains("Enfatiza su familia"));
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildEpisodeSummaryPrompt() {
        StepVerifier.create(prompts.episodeSummary("Treehouse of Horror"))
                .assertNext(result -> {
                    assertEquals("Resumen de episodio", result.description());
                    PromptMessage message = result.messages().getFirst();
                    assertEquals(Role.USER, message.role());
                    assertTrue(((TextContent) message.content()).text()
                            .contains("Treehouse of Horror"));
                })
                .verifyComplete();
    }
}

