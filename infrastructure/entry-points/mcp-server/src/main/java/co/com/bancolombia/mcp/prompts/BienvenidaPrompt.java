package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.util.List;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Prompt de bienvenida usando anotaciones MCP
 * <p>
 * Con @McpArg(required = false), Spring AI permite argumentos opcionales
 */
@Component
public class BienvenidaPrompt {

        @McpPrompt(name = "bienvenida", description = "Genera un prompt de bienvenida formal con título opcional")
        @PreAuthorize("hasAnyRole('MCP.PROMPT.BASIC', 'MCP.ADMIN')")
        public Mono<GetPromptResult> getBienvenidaPrompt(
                @McpArg(name = "titulo", required = false) String titulo) {
                return Mono.fromCallable(() -> {
                        String saludo = (titulo == null || titulo.isEmpty())
                                ? "Bienvenido/a"
                                : "Bienvenido/a, " + titulo;

                        String promptText = saludo
                                + ". Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?";

                        PromptMessage message = new PromptMessage(
                                Role.USER,
                                new TextContent(promptText));

                        return new GetPromptResult("Bienvenida formal", List.of(message));
                });
        }
}