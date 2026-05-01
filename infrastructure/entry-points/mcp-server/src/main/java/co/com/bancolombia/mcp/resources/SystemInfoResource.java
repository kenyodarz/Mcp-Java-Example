package co.com.bancolombia.mcp.resources;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Resource de información del sistema usando anotaciones MCP
 * <p>
 * Con @McpResource, Spring AI automáticamente: - Registra el resource con la
 * URI especificada -
 * Maneja las peticiones de lectura - Serializa la respuesta
 */
@Component
public class SystemInfoResource {

    private final JsonMapper objectMapper;

    public SystemInfoResource(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @McpResource(uri = "resource://system/info", name = "system-info", description = "Proporciona información básica del sistema y metadata del servidor MCP")
    @PreAuthorize("hasAnyRole('MCP.RESOURCE.SYSTEM.READ', 'MCP.ADMIN')")
    public Mono<ReadResourceResult> getSystemInfo() {
        return Mono.fromCallable(() -> {
            Map<String, Object> info = Map.of(
                    "service", "mcp-bancolombia",
                    "version", "1.0.0",
                    "status", "UP",
                    "reactive", true,
                    "capabilities", List.of("tools", "resources", "prompts"),
                    "timestamp", System.currentTimeMillis());

            return new ReadResourceResult(
                    List.of(new TextResourceContents(
                            "resource://system/info",
                            MediaType.APPLICATION_JSON_VALUE,
                            toJson(info))));
        });
    }

    @SneakyThrows
    private String toJson(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}