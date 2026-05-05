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

    private final JsonMapper mapper = JsonMapper.builder().build();
    @Mock
    private SimpsonsGateway simpsonsGateway;
    private SimpsonsResources resources;

    @BeforeEach
    void setUp() {
        resources = new SimpsonsResources(simpsonsGateway, mapper);
    }

    @Test
    void shouldReturnCharacterResourceAsJson() {
        SimpsonsCharacter character = SimpsonsCharacter.builder().id(1).name("Homer Simpson")
                .build();
        when(simpsonsGateway.getCharacterById(1)).thenReturn(Mono.just(character));

        StepVerifier.create(resources.getCharacterResource("1"))
                .assertNext(result -> {
                    TextResourceContents content = (TextResourceContents) result.contents()
                            .getFirst();
                    assertEquals("simpsons://character/1", content.uri());
                    JsonNode json = readJson(content.text());
                    assertEquals(1, json.get("id").asInt());
                    assertEquals("Homer Simpson", json.get("name").asText());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEpisodeResourceAsJson() {
        SimpsonsEpisode episode = SimpsonsEpisode.builder().id(2).name("Lisa's Substitute").build();
        when(simpsonsGateway.getEpisodeById(2)).thenReturn(Mono.just(episode));

        StepVerifier.create(resources.getEpisodeResource("2"))
                .assertNext(result -> {
                    TextResourceContents content = (TextResourceContents) result.contents()
                            .getFirst();
                    assertEquals("simpsons://episode/2", content.uri());
                    JsonNode json = readJson(content.text());
                    assertEquals(2, json.get("id").asInt());
                    assertEquals("Lisa's Substitute", json.get("name").asText());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnLocationResourceAsJson() {
        SimpsonsLocation location = SimpsonsLocation.builder().id(3).name("Kwik-E-Mart").build();
        when(simpsonsGateway.getLocationById(3)).thenReturn(Mono.just(location));

        StepVerifier.create(resources.getLocationResource("3"))
                .assertNext(result -> {
                    TextResourceContents content = (TextResourceContents) result.contents()
                            .getFirst();
                    assertEquals("simpsons://location/3", content.uri());
                    JsonNode json = readJson(content.text());
                    assertEquals(3, json.get("id").asInt());
                    assertEquals("Kwik-E-Mart", json.get("name").asText());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailFastForInvalidCharacterId() {
        StepVerifier.create(Mono.fromCallable(() -> resources.getCharacterResource("abc")))
                .expectError(NumberFormatException.class)
                .verify();
    }

    private JsonNode readJson(String value) {
        try {
            return mapper.readTree(value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}

