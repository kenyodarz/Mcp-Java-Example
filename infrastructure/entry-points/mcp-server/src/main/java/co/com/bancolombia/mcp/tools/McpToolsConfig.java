package co.com.bancolombia.mcp.tools;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class McpToolsConfig {

    private final HealthTool healthTool;

    public McpToolsConfig(HealthTool healthTool) {
        this.healthTool = healthTool;
        log.info("McpToolsConfig initialized with HealthTool: {}", healthTool);
    }

    @Bean
    public List<McpStatelessServerFeatures.AsyncToolSpecification> mcpTools() {
        log.info("Creating mcpTools bean...");

        McpSchema.Tool saludoTool = McpSchema.Tool.builder()
                .name("saludoTool")
                .title("Saludo Tool") // opcional
                .description("Devuelve un saludo reactivo")
                .inputSchema("""
                        {
                          "type": "object",
                          "properties": {
                            "name": { "type": "string" }
                          },
                          "required": ["name"]
                        }
                        """)
                .build();

        McpSchema.Tool healthCheckTool = McpSchema.Tool.builder()
                .name("healthCheck")
                .title("Health Check Tool") // opcional, puedes omitirlo si no quieres título distinto
                .description("Retorna 'OK' para health checks")
                .inputSchema("""
                        {
                          "type": "object",
                          "properties": {}
                        }
                        """)
                .build();

        return List.of(
                new McpStatelessServerFeatures.AsyncToolSpecification(
                        saludoTool,
                        (exchange, input) -> {
                            log.info("Executing saludoTool with input: {}", input);
                            return Mono.just(
                                    new McpSchema.CallToolResult("Hola desde MCP Tool: " + input,
                                            false)
                            );
                        }
                ),
                new McpStatelessServerFeatures.AsyncToolSpecification(
                        healthCheckTool,
                        (exchange, input) -> {
                            log.info("Executing healthCheck tool");
                            return Mono.just(
                                    new McpSchema.CallToolResult(healthTool.healthCheck(), false)
                            );
                        }
                )
        );
    }
}