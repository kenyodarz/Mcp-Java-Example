package co.com.bancolombia.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("HealthTool Unit Tests")
class HealthToolTest {

    private HealthTool healthTool;

    @BeforeEach
    void setUp() {
        healthTool = new HealthTool();
    }

    @Test
    @DisplayName("healthCheck debe retornar 'OK'")
    void healthCheck_shouldReturnOK() {
        // When
        String result = healthTool.healthCheck();

        // Then
        assertThat(result).isEqualTo("OK");
    }

    @Test
    @DisplayName("getToolSpecification debe retornar una especificación válida")
    void getToolSpecification_shouldReturnValidSpecification() {
        // When
        McpStatelessServerFeatures.AsyncToolSpecification spec = healthTool.getToolSpecification();

        // Then
        assertNotNull(spec, "La especificación no debe ser nula");
        assertNotNull(spec.tool(), "El tool no debe ser nulo");
        assertNotNull(spec.callHandler(), "El handler no debe ser nulo");
    }

    @Test
    @DisplayName("getToolSpecification debe configurar correctamente el nombre de la herramienta")
    void getToolSpecification_shouldConfigureToolNameCorrectly() {
        // When
        McpStatelessServerFeatures.AsyncToolSpecification spec = healthTool.getToolSpecification();

        // Then
        assertThat(spec.tool().name()).isEqualTo("healthCheck");
    }

    @Test
    @DisplayName("getToolSpecification debe configurar correctamente el título de la herramienta")
    void getToolSpecification_shouldConfigureToolTitleCorrectly() {
        // When
        McpStatelessServerFeatures.AsyncToolSpecification spec = healthTool.getToolSpecification();

        // Then
        assertThat(spec.tool().title()).isEqualTo("Health Check Tool");
    }

    @Test
    @DisplayName("getToolSpecification debe configurar correctamente la descripción")
    void getToolSpecification_shouldConfigureDescriptionCorrectly() {
        // When
        McpStatelessServerFeatures.AsyncToolSpecification spec = healthTool.getToolSpecification();

        // Then
        assertThat(spec.tool().description()).isEqualTo("Retorna 'OK' para health checks");
    }

    @Test
    @DisplayName("getToolSpecification debe configurar un inputSchema vacío")
    void getToolSpecification_shouldConfigureEmptyInputSchema() {
        // When
        McpStatelessServerFeatures.AsyncToolSpecification spec = healthTool.getToolSpecification();
        McpSchema.JsonSchema inputSchema = spec.tool().inputSchema();

        // Then
        assertNotNull(inputSchema, "El inputSchema no debe ser nulo");
        assertEquals("object", inputSchema.type(), "El tipo del schema debe ser 'object'");
        assertNotNull(inputSchema.properties(), "Las propiedades no deben ser nulas");
        assertTrue(inputSchema.properties().isEmpty(), "Las propiedades deben estar vacías");
    }


    @Test
    @DisplayName("El handler de la especificación debe retornar CallToolResult con 'OK'")
    void toolSpecificationHandler_shouldReturnCallToolResultWithOK() {
        // Given
        McpStatelessServerFeatures.AsyncToolSpecification spec = healthTool.getToolSpecification();

        // When & Then
        StepVerifier.create(spec.callHandler().apply(null, null))
                .assertNext(result -> {
                    assertThat(result).isInstanceOf(McpSchema.CallToolResult.class);
                    McpSchema.CallToolResult callResult = (McpSchema.CallToolResult) result;
                    assertThat(callResult.isError()).isFalse();

                    // El content es una lista de TextContent
                    assertThat(callResult.content()).isNotNull();
                    assertThat(callResult.content()).isNotEmpty();

                    // Verificar el primer TextContent
                    Object firstContent = callResult.content().get(0);
                    assertThat(firstContent).isInstanceOf(McpSchema.TextContent.class);

                    McpSchema.TextContent textContent = (McpSchema.TextContent) firstContent;
                    assertThat(textContent.text()).isEqualTo("OK");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("El handler debe funcionar correctamente sin exchange ni input")
    void toolSpecificationHandler_shouldWorkWithoutExchangeAndInput() {
        // Given
        McpStatelessServerFeatures.AsyncToolSpecification spec = healthTool.getToolSpecification();

        // When & Then
        StepVerifier.create(spec.callHandler().apply(null, null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Múltiples llamadas a healthCheck deben retornar el mismo resultado")
    void healthCheck_shouldReturnConsistentResults() {
        // When
        String result1 = healthTool.healthCheck();
        String result2 = healthTool.healthCheck();
        String result3 = healthTool.healthCheck();

        // Then
        assertThat(result1).isEqualTo("OK");
        assertThat(result2).isEqualTo("OK");
        assertThat(result3).isEqualTo("OK");
    }

    @Test
    @DisplayName("Múltiples llamadas a getToolSpecification deben generar handlers funcionales")
    void getToolSpecification_shouldGenerateFunctionalHandlersMultipleTimes() {
        // Given
        McpStatelessServerFeatures.AsyncToolSpecification spec1 = healthTool.getToolSpecification();
        McpStatelessServerFeatures.AsyncToolSpecification spec2 = healthTool.getToolSpecification();

        // When & Then
        StepVerifier.create(spec1.callHandler().apply(null, null))
                .assertNext(result -> {
                    McpSchema.TextContent textContent = (McpSchema.TextContent) ((McpSchema.CallToolResult) result).content()
                            .getFirst();
                    assertThat(textContent.text()).isEqualTo("OK");
                })
                .verifyComplete();

        StepVerifier.create(spec2.callHandler().apply(null, null))
                .assertNext(result -> {
                    McpSchema.TextContent textContent = (McpSchema.TextContent) ((McpSchema.CallToolResult) result).content()
                            .getFirst();
                    assertThat(textContent.text()).isEqualTo("OK");
                })
                .verifyComplete();
    }
}