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
public class BienvenidaPrompt {

    public McpStatelessServerFeatures.AsyncPromptSpecification getPromptSpecification() {
        var promptMeta = new McpSchema.Prompt(
                "bienvenida",
                "Un prompt de bienvenida formal",
                List.of(
                        new McpSchema.PromptArgument("titulo",
                                "Título de la persona (Sr., Sra., etc.)", false))
        );

        return new McpStatelessServerFeatures.AsyncPromptSpecification(
                promptMeta,
                (exchange, req) -> {
                    String titulo = (String) req.arguments().getOrDefault("titulo", "");
                    String saludo = titulo.isEmpty() ? "Bienvenido/a" : "Bienvenido/a, " + titulo;
                    PromptMessage userMsg = new PromptMessage(Role.USER,
                            new TextContent(saludo
                                    + ". Gracias por usar nuestro servicio. ¿Cómo podemos asistirle?"));
                    return Mono.just(
                            new McpSchema.GetPromptResult("Bienvenida formal", List.of(userMsg))
                    );
                }
        );
    }
}