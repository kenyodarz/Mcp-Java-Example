package co.com.bancolombia.mcp.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import co.com.bancolombia.model.simpsons.SimpsonsCharacter;
import co.com.bancolombia.model.simpsons.SimpsonsEpisode;
import co.com.bancolombia.model.simpsons.SimpsonsLocation;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SimpsonsResourcesTest {

    // ==================== TEST DOUBLES ====================
    // Mapper para serializar/deserializar JSON
    private final JsonMapper jsonMapperDouble = JsonMapper.builder().build();

    // Mock del gateway que proporciona datos de Simpsons
    @Mock
    private SimpsonsGateway simpsonsGatewayMock;

    // Sistema bajo prueba (SUT): El recurso MCP que expone datos de Simpsons en formato JSON
    private SimpsonsResources simpsonsResourcesSUT;

    @BeforeEach
    void setUp() {
        // Inicializar el recurso con el mock del gateway y mapper
        simpsonsResourcesSUT = new SimpsonsResources(simpsonsGatewayMock, jsonMapperDouble);
    }

    @Test
    void shouldReturnCharacterResourceAsJson() {
        // ==================== GIVEN ====================
        // Preparar un character esperado
        SimpsonsCharacter expectedCharacter = SimpsonsCharacter.builder()
                .id(1)
                .name("Homer Simpson")
                .build();

        // Configurar el mock del gateway
        when(simpsonsGatewayMock.getCharacterById(1)).thenReturn(Mono.just(expectedCharacter));

        // ==================== WHEN ====================
        // Ejecutar la solicitud del recurso para obtener el character

        // ==================== THEN ====================
        // Verificar que el recurso retorna el character en formato JSON
        StepVerifier.create(simpsonsResourcesSUT.getCharacterResource("1"))
                .assertNext(readResourceResult -> {
                    TextResourceContents textResourceContent = (TextResourceContents) readResourceResult.contents()
                            .getFirst();
                    assertEquals("simpsons://character/1", textResourceContent.uri());
                    JsonNode characterJsonNode = readJsonFromString(textResourceContent.text());
                    assertEquals(1, characterJsonNode.get("id").asInt());
                    assertEquals("Homer Simpson", characterJsonNode.get("name").asText());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEpisodeResourceAsJson() {
        // ==================== GIVEN ====================
        // Preparar un episode esperado
        SimpsonsEpisode expectedEpisode = SimpsonsEpisode.builder()
                .id(2)
                .name("Lisa's Substitute")
                .build();

        // Configurar el mock del gateway
        when(simpsonsGatewayMock.getEpisodeById(2)).thenReturn(Mono.just(expectedEpisode));

        // ==================== WHEN ====================
        // Ejecutar la solicitud del recurso para obtener el episode

        // ==================== THEN ====================
        // Verificar que el recurso retorna el episode en formato JSON
        StepVerifier.create(simpsonsResourcesSUT.getEpisodeResource("2"))
                .assertNext(readResourceResult -> {
                    TextResourceContents textResourceContent = (TextResourceContents) readResourceResult.contents()
                            .getFirst();
                    assertEquals("simpsons://episode/2", textResourceContent.uri());
                    JsonNode episodeJsonNode = readJsonFromString(textResourceContent.text());
                    assertEquals(2, episodeJsonNode.get("id").asInt());
                    assertEquals("Lisa's Substitute", episodeJsonNode.get("name").asText());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnLocationResourceAsJson() {
        // ==================== GIVEN ====================
        // Preparar una location esperada
        SimpsonsLocation expectedLocation = SimpsonsLocation.builder()
                .id(3)
                .name("Kwik-E-Mart")
                .build();

        // Configurar el mock del gateway
        when(simpsonsGatewayMock.getLocationById(3)).thenReturn(Mono.just(expectedLocation));

        // ==================== WHEN ====================
        // Ejecutar la solicitud del recurso para obtener la location

        // ==================== THEN ====================
        // Verificar que el recurso retorna la location en formato JSON
        StepVerifier.create(simpsonsResourcesSUT.getLocationResource("3"))
                .assertNext(readResourceResult -> {
                    TextResourceContents textResourceContent = (TextResourceContents) readResourceResult.contents()
                            .getFirst();
                    assertEquals("simpsons://location/3", textResourceContent.uri());
                    JsonNode locationJsonNode = readJsonFromString(textResourceContent.text());
                    assertEquals(3, locationJsonNode.get("id").asInt());
                    assertEquals("Kwik-E-Mart", locationJsonNode.get("name").asText());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailFastForInvalidCharacterId() {
        // ==================== GIVEN ====================
        // Preparar un ID de character inválido (no es número)
        String invalidCharacterId = "abc";

        // ==================== WHEN ====================
        // Ejecutar la solicitud del recurso con ID inválido

        // ==================== THEN ====================
        // Verificar que falla rápidamente con excepción de formato numérico
        StepVerifier.create(Mono.fromCallable(
                        () -> simpsonsResourcesSUT.getCharacterResource(invalidCharacterId)))
                .expectError(NumberFormatException.class)
                .verify();
    }

    private JsonNode readJsonFromString(String jsonString) {
        try {
            return jsonMapperDouble.readTree(jsonString);
        } catch (Exception jsonParsingException) {
            throw new RuntimeException(jsonParsingException);
        }
    }
}

