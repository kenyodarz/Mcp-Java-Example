package co.com.bancolombia.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class SystemInfoResource {

    private final ObjectMapper objectMapper;

    public SystemInfoResource(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AsyncResourceSpecification getResourceSpecification() {
        var systemResource = McpSchema.Resource.builder()
                .uri("resource://system/info")
                .name("system-info")
                .title("System information")
                .description("Provides basic system health and metadata")
                .mimeType(MediaType.APPLICATION_JSON_VALUE)
                .build();

        return new AsyncResourceSpecification(
                systemResource,
                (McpTransportContext ctx, McpSchema.ReadResourceRequest request) ->
                        Mono.fromCallable(() -> {
                            Map<String, Object> info = Map.of(
                                    "service", "mcp-bancolombia",
                                    "status", "UP",
                                    "reactive", true
                            );
                            return new McpSchema.ReadResourceResult(
                                    List.of(new McpSchema.TextResourceContents(
                                            request.uri(),
                                            MediaType.APPLICATION_JSON_VALUE,
                                            objectMapper.writeValueAsString(info)
                                    ))
                            );
                        }).subscribeOn(Schedulers.boundedElastic())
        );
    }
}