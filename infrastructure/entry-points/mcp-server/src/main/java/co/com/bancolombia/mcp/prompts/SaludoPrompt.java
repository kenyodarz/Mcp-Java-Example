package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SaludoPrompt {

    public McpStatelessServerFeatures.AsyncPromptSpecification getPromptSpecification() {

        var promptMeta = new McpSchema.Prompt(
                "saludo",
                "Un prompt de saludo personalizable",
                List.of(new McpSchema.PromptArgument("nombre", "Nombre a saludar", true))
        );

        return new McpStatelessServerFeatures.AsyncPromptSpecification(
                promptMeta,
                (exchange, req) -> {
                    String nombre = (String) req.arguments().getOrDefault("nombre", "amigo");
                    PromptMessage userMsg = new PromptMessage(Role.USER,
                            new TextContent("Hola " + nombre + ", ¿en qué te ayudo?"));
                    return Mono.just(
                            new McpSchema.GetPromptResult("Saludo base", List.of(userMsg))
                    );
                }
        );
    }
}