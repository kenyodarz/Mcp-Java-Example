package co.com.bancolombia.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
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
    public List<McpServerFeatures.AsyncToolSpecification> mcpTools() {
        log.info("Creating mcpTools bean...");

        McpSchema.Tool saludoTool = new McpSchema.Tool(
                "saludoTool",
                "Devuelve un saludo reactivo",
                """
                {
                  "type": "object",
                  "properties": {
                    "name": { "type": "string" }
                  },
                  "required": ["name"]
                }
                """
        );


        McpSchema.Tool healthCheckTool = new McpSchema.Tool(
                "healthCheck",
                "Retorna 'OK' para health checks",
                """
                {
                  "type": "object",
                  "properties": {}
                }
                """
        );


        return List.of(
                new McpServerFeatures.AsyncToolSpecification(
                        saludoTool,
                        (exchange, input) -> {
                            log.info("Executing saludoTool with input: {}", input);
                            return Mono.just(
                                    new McpSchema.CallToolResult("Hola desde MCP Tool: " + input,
                                            false)
                            );
                        }
                ),
                new McpServerFeatures.AsyncToolSpecification(
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