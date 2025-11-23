package co.com.bancolombia.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("SystemInfoResource Unit Tests")
class SystemInfoResourceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SystemInfoResource resource = new SystemInfoResource(mapper);

    @Test
    @DisplayName("Debe retornar información del sistema en formato JSON dentro de ReadResourceResult")
    void shouldReturnSystemInfo() {

        var resultMono = resource.getSystemInfo();

        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    var contents = result.contents();

                    assert contents.size() == 1;

                    ResourceContents content = contents.getFirst();
                    assert content instanceof TextResourceContents;

                    TextResourceContents textContent = (TextResourceContents) content;

                    assert textContent.uri().equals("resource://system/info");
                    assert textContent.mimeType().equals("application/json");

                    // Verifica que el JSON sea válido (no importa el orden)
                    try {
                        mapper.readTree(textContent.text());
                    } catch (Exception e) {
                        assert false : "El JSON no es válido";
                    }
                })
                .verifyComplete();
    }
}