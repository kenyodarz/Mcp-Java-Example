package co.com.bancolombia.mcp.tools;

import co.com.bancolombia.model.simpsons.SimpsonsCharacter;
import co.com.bancolombia.model.simpsons.SimpsonsEpisode;
import co.com.bancolombia.model.simpsons.SimpsonsLocation;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SimpsonsTools {

    private final SimpsonsGateway simpsonsGateway;

    @McpTool(name = "get_character", description = "Obtiene información detallada de un personaje de Los Simpsons por su ID")
    @PreAuthorize("hasAnyRole('MCP.TOOL.SIMPSONS', 'MCP.ADMIN')")
    public Mono<SimpsonsCharacter> getCharacter(
            @McpToolParam(description = "ID del personaje") int id) {
        return simpsonsGateway.getCharacterById(id);
    }

    @McpTool(name = "get_episode", description = "Obtiene información detallada de un episodio de Los Simpsons por su ID")
    @PreAuthorize("hasAnyRole('MCP.TOOL.SIMPSONS', 'MCP.ADMIN')")
    public Mono<SimpsonsEpisode> getEpisode(
            @McpToolParam(description = "ID del episodio") int id) {
        return simpsonsGateway.getEpisodeById(id);
    }

    @McpTool(name = "get_location", description = "Obtiene información detallada de una ubicación de Springfield por su ID")
    @PreAuthorize("hasAnyRole('MCP.TOOL.SIMPSONS', 'MCP.ADMIN')")
    public Mono<SimpsonsLocation> getLocation(
            @McpToolParam(description = "ID de la ubicación") int id) {
        return simpsonsGateway.getLocationById(id);
    }
}
