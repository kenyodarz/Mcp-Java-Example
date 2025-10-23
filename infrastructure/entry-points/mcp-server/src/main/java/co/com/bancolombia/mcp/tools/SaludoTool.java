package co.com.bancolombia.mcp.tools;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SaludoTool {

    @Tool(name = "saludoTool", description = "Devuelve un saludo reactivo")
    public String saludo(String name) {
        return "Hola desde MCP Tool: " + name;
    }

    public McpStatelessServerFeatures.AsyncToolSpecification getToolSpecification() {
        McpSchema.Tool saludoTool = McpSchema.Tool.builder()
                .name("saludoTool")
                .title("Saludo Tool")
                .description("Devuelve un saludo reactivo")
                .inputSchema(new McpSchema.JsonSchema(
                        "object", // type
                        Map.of(   // properties
                                "name", Map.of(
                                        "type", "string",
                                        "description", "Nombre de la persona a saludar"
                                )
                        ),
                        List.of("name"), // required
                        false,           // additionalProperties
                        Map.of(),        // $defs
                        Map.of()         // definitions
                ))
                .build();

        return new McpStatelessServerFeatures.AsyncToolSpecification(
                saludoTool,
                (exchange, input) -> {
                    // Aquí asumimos que 'input' contiene el JSON con el campo "name"
                    String name = extractNameFromInput(String.valueOf(input));
                    return Mono.just(
                            new McpSchema.CallToolResult(saludo(name), false)
                    );
                }
        );
    }

    // Metodo auxiliar para extraer el nombre del input (puedes ajustarlo según tu lógica)
    private String extractNameFromInput(String input) {
        // Implementa la lógica para parsear el JSON del input y extraer el campo "name"
        // Por ejemplo, usando un parser JSON como Jackson o similar
        return input;
    }
}