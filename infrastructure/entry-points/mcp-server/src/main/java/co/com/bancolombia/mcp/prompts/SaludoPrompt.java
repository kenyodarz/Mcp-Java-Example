package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.util.List;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Prompt de saludo usando anotaciones MCP
 * <p>
 * Con @McpPrompt y @McpArg, Spring AI automáticamente: - Registra el prompt con sus argumentos -
 * Genera el schema de argumentos - Valida los argumentos requeridos
 */
@Component
public class SaludoPrompt {

    @McpPrompt(
            name = "saludo",
            description = "Genera un prompt de saludo personalizable"
    )
    public Mono<GetPromptResult> getSaludoPrompt(
            @McpArg(name = "nombre", required = true) String nombre
    ) {
        return Mono.fromCallable(() -> {

            /*
             * Nota Importante:
             * Aunque @McpArg(required = true) valida que 'nombre' no sea null,
             *  es recomendable agregar validaciones adicionales según las reglas de negocio.
             * @McpArg(required = true) solo valida cuando la llamada viene del runtime MCP,
             *  no cuando tú llamas el metodo directamente desde código o tests.
             */
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }

            String promptText = String.format("Hola %s, ¿en qué te ayudo?", nombre);

            PromptMessage message = new PromptMessage(
                    Role.USER,
                    new TextContent(promptText)
            );

            return new GetPromptResult("Saludo base", List.of(message));
        });
    }
}