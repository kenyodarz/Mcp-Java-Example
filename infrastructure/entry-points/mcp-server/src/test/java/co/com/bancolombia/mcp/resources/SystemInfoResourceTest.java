package co.com.bancolombia.mcp.resources;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("SystemInfoResource Unit Tests")
class SystemInfoResourceTest {

    // ==================== TEST DOUBLES ====================
    // Mapper para serializar/deserializar JSON
    private final JsonMapper jsonMapperDouble = JsonMapper.builder().build();

    // Sistema bajo prueba (SUT): El recurso MCP que expone la información del sistema
    private final SystemInfoResource systemInfoResourceSUT = new SystemInfoResource(
            jsonMapperDouble);

    @Test
    @DisplayName("Debe retornar información del sistema en formato JSON dentro de ReadResourceResult")
    void shouldReturnSystemInfo() {
        // ==================== GIVEN ====================
        // No hay preparación adicional necesaria para obtener información del sistema

        // ==================== WHEN ====================
        // Ejecutar la solicitud del recurso para obtener la información del sistema
        var systemInfoResourceResultMono = systemInfoResourceSUT.getSystemInfo();

        // ==================== THEN ====================
        // Verificar que el recurso retorna información del sistema válida en JSON
        StepVerifier.create(systemInfoResourceResultMono)
                .assertNext(readResourceResult -> {
                    var resourceContentsList = readResourceResult.contents();

                    assert resourceContentsList.size() == 1;

                    ResourceContents resourceContent = resourceContentsList.getFirst();
                    assert resourceContent instanceof TextResourceContents;

                    TextResourceContents textResourceContent = (TextResourceContents) resourceContent;

                    assert textResourceContent.uri().equals("resource://system/info");
                    assert textResourceContent.mimeType().equals("application/json");

                    // Verificar que el JSON es válido (no importa el orden)
                    try {
                        jsonMapperDouble.readTree(textResourceContent.text());
                    } catch (Exception jsonParsingException) {
                        assert false : "El JSON no es válido: " + jsonParsingException.getMessage();
                    }
                })
                .verifyComplete();
    }
}