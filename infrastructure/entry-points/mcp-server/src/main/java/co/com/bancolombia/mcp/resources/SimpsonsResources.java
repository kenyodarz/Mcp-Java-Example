package co.com.bancolombia.mcp.resources;

import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SimpsonsResources {

    private final SimpsonsGateway simpsonsGateway;
    private final ObjectMapper objectMapper;

    @McpResource(uri = "simpsons://character/{id}", name = "simpsons-character", description = "Recurso que representa un personaje de Los Simpsons")
    @PreAuthorize("hasAnyRole('MCP.RESOURCE.SIMPSONS', 'MCP.ADMIN')")
    public Mono<ReadResourceResult> getCharacterResource(String id) {
        return simpsonsGateway.getCharacterById(Integer.parseInt(id))
                .map(character -> createResourceResult("simpsons://character/" + id, character));
    }

    @McpResource(uri = "simpsons://episode/{id}", name = "simpsons-episode", description = "Recurso que representa un episodio de Los Simpsons")
    @PreAuthorize("hasAnyRole('MCP.RESOURCE.SIMPSONS', 'MCP.ADMIN')")
    public Mono<ReadResourceResult> getEpisodeResource(String id) {
        return simpsonsGateway.getEpisodeById(Integer.parseInt(id))
                .map(episode -> createResourceResult("simpsons://episode/" + id, episode));
    }

    @McpResource(uri = "simpsons://location/{id}", name = "simpsons-location", description = "Recurso que representa una ubicación en Springfield")
    @PreAuthorize("hasAnyRole('MCP.RESOURCE.SIMPSONS', 'MCP.ADMIN')")
    public Mono<ReadResourceResult> getLocationResource(String id) {
        return simpsonsGateway.getLocationById(Integer.parseInt(id))
                .map(location -> createResourceResult("simpsons://location/" + id, location));
    }

    private ReadResourceResult createResourceResult(String uri, Object content) {
        return new ReadResourceResult(
                List.of(new TextResourceContents(
                        uri,
                        MediaType.APPLICATION_JSON_VALUE,
                        toJson(content))));
    }

    @SneakyThrows
    private String toJson(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
