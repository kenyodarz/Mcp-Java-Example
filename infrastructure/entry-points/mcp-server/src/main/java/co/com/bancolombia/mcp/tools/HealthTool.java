package co.com.bancolombia.mcp.tools;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class HealthTool {

    @Tool(name = "healthCheck", description = "Retorna 'OK' para health checks")
    public String healthCheck() {
        return "OK";
    }

    // Metodo para obtener la especificación de la herramienta
    public McpStatelessServerFeatures.AsyncToolSpecification getToolSpecification() {
        McpSchema.Tool healthCheckTool = McpSchema.Tool.builder()
                .name("healthCheck")
                .title("Health Check Tool")
                .description("Retorna 'OK' para health checks")
                .inputSchema(new McpSchema.JsonSchema(
                        "object", // type
                        Map.of(), // properties
                        List.of(), // required
                        false, // additionalProperties
                        Map.of(), // $defs
                        Map.of()  // definitions
                ))
                .build();

        return new McpStatelessServerFeatures.AsyncToolSpecification(
                healthCheckTool,
                (exchange, input) -> {
                    // Puedes agregar logs aquí si es necesario
                    return Mono.just(
                            new McpSchema.CallToolResult(healthCheck(), false)
                    );
                }
        );
    }
}