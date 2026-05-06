package co.com.bancolombia.mcp.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class SimpsonsPromptsTest {

    // ==================== TEST DOUBLES & SUT ====================
    // Sistema bajo prueba (SUT): Las prompts MCP para Simpsons
    private final SimpsonsPrompts simpsonsPromptsSUT = new SimpsonsPrompts();

    @Test
    void shouldBuildCharacterProfilePrompt() {
        // ==================== GIVEN ====================
        // Preparar los parámetros para construir el prompt de perfil de character
        String characterName = "Homer Simpson";
        String characterProfile = "Enfatiza su familia";

        // ==================== WHEN ====================
        // Ejecutar la generación del prompt de perfil

        // ==================== THEN ====================
        // Verificar que el prompt se construye correctamente con la descripción y mensajes
        StepVerifier.create(simpsonsPromptsSUT.characterProfile(characterName, characterProfile))
                .assertNext(promptResult -> {
                    assertEquals("Perfil de personaje", promptResult.description());
                    PromptMessage promptMessage = promptResult.messages().getFirst();
                    assertEquals(Role.USER, promptMessage.role());
                    assertTrue(
                            ((TextContent) promptMessage.content()).text().contains(characterName));
                    assertTrue(((TextContent) promptMessage.content()).text()
                            .contains(characterProfile));
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildEpisodeSummaryPrompt() {
        // ==================== GIVEN ====================
        // Preparar el parámetro para construir el prompt de resumen de episode
        String episodeName = "Treehouse of Horror";

        // ==================== WHEN ====================
        // Ejecutar la generación del prompt de resumen

        // ==================== THEN ====================
        // Verificar que el prompt se construye correctamente con la descripción y mensajes
        StepVerifier.create(simpsonsPromptsSUT.episodeSummary(episodeName))
                .assertNext(promptResult -> {
                    assertEquals("Resumen de episodio", promptResult.description());
                    PromptMessage promptMessage = promptResult.messages().getFirst();
                    assertEquals(Role.USER, promptMessage.role());
                    assertTrue(((TextContent) promptMessage.content()).text()
                            .contains(episodeName));
                })
                .verifyComplete();
    }
}

