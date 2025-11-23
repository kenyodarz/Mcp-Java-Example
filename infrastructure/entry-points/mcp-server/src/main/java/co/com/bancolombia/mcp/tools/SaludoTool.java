package co.com.bancolombia.mcp.tools;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
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
@Slf4j
@Component
public class SaludoTool {

    @McpTool(
            name = "saludoTool",
            description = "Genera un saludo personalizado reactivo para el usuario"
    )
    public Mono<String> saludo(
            @McpToolParam(description = "Nombre de la persona a saludar", required = true)
            String name
    ) {
        return Mono.fromCallable(() -> {
            if (name == null || name.trim().isEmpty()) {
                log.warn("Intento de saludo con nombre vacío");
                return "¡Hola! ¿Cómo te llamas?";
            }

            String greeting = String.format(
                    "¡Hola %s! Bienvenido al servidor MCP de Bancolombia. ¿En qué puedo ayudarte hoy?",
                    name.trim()
            );

            log.info("Saludo generado para: {}", name);
            return greeting;
        });
    }
}
