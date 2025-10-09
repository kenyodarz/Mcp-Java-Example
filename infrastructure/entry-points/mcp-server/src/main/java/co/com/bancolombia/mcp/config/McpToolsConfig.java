package co.com.bancolombia.mcp.config;

import co.com.bancolombia.mcp.tools.HealthTool;
import co.com.bancolombia.mcp.tools.SaludoTool;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class McpToolsConfig {

    private final HealthTool healthTool;
    private final SaludoTool saludoTool; // Suponiendo que existe

    public McpToolsConfig(HealthTool healthTool, SaludoTool saludoTool) {
        this.healthTool = healthTool;
        this.saludoTool = saludoTool;
        log.info("McpToolsConfig initialized with HealthTool: {} and SaludoTool: {}", healthTool,
                saludoTool);
    }

    @Bean
    public List<McpStatelessServerFeatures.AsyncToolSpecification> mcpTools() {
        log.info("Creating mcpTools bean...");
        return List.of(
                saludoTool.getToolSpecification(),
                healthTool.getToolSpecification()
        );
    }
}