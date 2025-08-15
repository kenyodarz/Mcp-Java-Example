package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class McpPromptsConfig {

    @Bean
    public List<McpServerFeatures.AsyncPromptSpecification> prompts() {
        var promptMeta = new McpSchema.Prompt(
                "saludo",
                "Un prompt de saludo personalizable",
                List.of(new McpSchema.PromptArgument("nombre", "Nombre a saludar", true))
        );

        var asyncSpec = new McpServerFeatures.AsyncPromptSpecification(
                promptMeta,
                (exchange, req) -> {
                    String nombre = (String) req.arguments().getOrDefault("nombre", "amigo");
                    PromptMessage userMsg = new PromptMessage(Role.USER,
                            new TextContent("Hola " + nombre + ", ¿en qué te ayudo?"));
                    return Mono.just(new McpSchema.GetPromptResult("Saludo base", List.of(userMsg)));
                }
        );

        return List.of(asyncSpec);
    }
}
