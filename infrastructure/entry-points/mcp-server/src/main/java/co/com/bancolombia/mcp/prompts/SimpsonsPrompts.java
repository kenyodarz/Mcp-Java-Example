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

@Component
public class SimpsonsPrompts {

    @McpPrompt(name = "perfil_personaje", description = "Genera un perfil detallado de un personaje de Los Simpsons")
    @PreAuthorize("hasAnyRole('MCP.PROMPT.SIMPSONS', 'MCP.ADMIN')")
    public Mono<GetPromptResult> characterProfile(
            @McpArg(name = "nombre", description = "Nombre del personaje") String name,
            @McpArg(name = "detalle", description = "Información adicional a incluir") String detail) {
        return Mono.fromCallable(() -> {
            String promptText = String.format("Genera un perfil completo para el personaje '%s'. " +
                    "Incluye detalles sobre su ocupación, familia y frases icónicas. " +
                    "Contexto adicional: %s", name, detail);

            PromptMessage message = new PromptMessage(
                    Role.USER,
                    new TextContent(promptText));

            return new GetPromptResult("Perfil de personaje", List.of(message));
        });
    }

    @McpPrompt(name = "resumen_episodio", description = "Solicita un resumen de un episodio específico")
    @PreAuthorize("hasAnyRole('MCP.PROMPT.SIMPSONS', 'MCP.ADMIN')")
    public Mono<GetPromptResult> episodeSummary(
            @McpArg(name = "episodio", description = "Nombre o número del episodio") String episode) {
        return Mono.fromCallable(() -> {
            String promptText = String
                    .format("Resume el episodio '%s' de Los Simpsons, destacando la trama principal y la subtrama.",
                            episode);

            PromptMessage message = new PromptMessage(
                    Role.USER,
                    new TextContent(promptText));

            return new GetPromptResult("Resumen de episodio", List.of(message));
        });
    }
}
