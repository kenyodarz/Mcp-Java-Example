package co.com.bancolombia.mcp.tools;

import co.com.bancolombia.usecase.SaludoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Tool de Saludo usando anotaciones MCP
 *
 * Con @McpTool y @McpToolParam, Spring AI automáticamente:
 * - Genera el JSON schema con las propiedades del parámetro
 * - Valida que los parámetros requeridos estén presentes
 * - Convierte automáticamente los tipos de datos
 */
@Component
@RequiredArgsConstructor
public class SaludoTool {

    private final SaludoUseCase saludoUseCase;

    @McpTool(name = "saludoTool", description = "Genera un saludo personalizado reactivo para el usuario")
    @PreAuthorize("hasAnyRole('MCP.TOOL.INTERACTION', 'MCP.ADMIN')")
    public Mono<String> saludo(
            @McpToolParam(description = "Nombre de la persona a saludar", required = true) String name) {
        return saludoUseCase.execute(name);
    }
}
