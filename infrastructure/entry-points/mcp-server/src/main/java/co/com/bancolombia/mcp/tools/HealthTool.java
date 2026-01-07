package co.com.bancolombia.mcp.tools;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Tool de Health Check usando anotaciones MCP
 * <p>
 * Con @McpTool, Spring AI automáticamente: - Genera el JSON schema - Registra
 * el tool en el
 * servidor MCP - Maneja la serialización/deserialización
 */
@Component
public class HealthTool {

    @McpTool(name = "healthCheck", description = "Verifica el estado del servidor MCP. Retorna 'OK' si todo funciona correctamente.")
    @PreAuthorize("hasAnyRole('MCP.TOOL.HEALTH', 'MCP.ADMIN')")
    public reactor.core.publisher.Mono<String> healthCheck() {
        return reactor.core.publisher.Mono.just("OK");
    }
}
