package co.com.bancolombia.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Tool de saludo personalizado con manejo mejorado de errores
 */
@Slf4j
@Component
public class SaludoTool {

    private final ObjectMapper objectMapper;

    public SaludoTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.info("SaludoTool inicializado correctamente");
    }

    /**
     * Metodo de negocio que genera el saludo
     */
    public String saludo(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Intento de saludo con nombre vacío");
            return "Hola! ¿Cómo te llamas?";
        }

        String greeting = String.format(
                "¡Hola %s! Bienvenido al servidor MCP de Bancolombia. ¿En qué puedo ayudarte hoy?",
                name.trim());
        log.debug("Saludo generado para: {}", name);
        return greeting;
    }

    /**
     * Especificación del tool para el servidor MCP
     */
    public McpStatelessServerFeatures.AsyncToolSpecification getToolSpecification() {
        log.debug("Creando especificación para SaludoTool");

        // Definición del schema de entrada
        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                "object",
                Map.of(
                        "name", Map.of(
                                "type", "string",
                                "description", "Nombre de la persona a saludar",
                                "minLength", 1,
                                "maxLength", 100
                        )
                ),
                List.of("name"),
                false,
                Map.of(),
                Map.of()
        );

        // Definición del tool
        McpSchema.Tool saludoTool = McpSchema.Tool.builder()
                .name("saludoTool")
                .title("Herramienta de Saludo")
                .description("Genera un saludo personalizado reactivo para el usuario")
                .inputSchema(inputSchema)
                .build();

        // Handler reactivo del tool
        return new McpStatelessServerFeatures.AsyncToolSpecification(
                saludoTool,
                (exchange, input) -> {
                    log.info("Ejecutando SaludoTool con input: {}", input);

                    return Mono.fromCallable(() -> {
                        try {
                            // Extraer el nombre del input
                            String name = extractNameFromInput(input);

                            // Generar el saludo
                            String greeting = saludo(name);

                            // Retornar resultado exitoso
                            return new McpSchema.CallToolResult(greeting, false);

                        } catch (Exception e) {
                            log.error("Error al procesar SaludoTool", e);
                            return new McpSchema.CallToolResult(
                                    "Error al generar el saludo: " + e.getMessage(),
                                    true
                            );
                        }
                    });
                }
        );
    }

    /**
     * Extrae el nombre del input del tool
     */
    private String extractNameFromInput(Object input) {
        try {
            if (input == null) {
                return "";
            }

            // Si es un Map, extraer directamente
            if (input instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inputMap = (Map<String, Object>) input;
                Object nameObj = inputMap.get("name");
                return nameObj != null ? nameObj.toString() : "";
            }

            // Si es String (JSON), parsear
            if (input instanceof String) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inputMap = objectMapper.readValue(
                        input.toString(),
                        Map.class
                );
                Object nameObj = inputMap.get("name");
                return nameObj != null ? nameObj.toString() : "";
            }

            // Fallback: intentar convertir a String
            return input.toString();

        } catch (Exception e) {
            log.error("Error al extraer nombre del input", e);
            return "";
        }
    }
}