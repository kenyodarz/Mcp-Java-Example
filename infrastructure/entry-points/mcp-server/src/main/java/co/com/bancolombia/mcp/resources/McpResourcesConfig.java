package co.com.bancolombia.mcp.resources;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Configuration
public class McpResourcesConfig {

    @Bean
    public List<AsyncResourceSpecification> asyncResources(ObjectMapper objectMapper) {

        // system-info (fijo)
        var systemResource = McpSchema.Resource.builder()
                .uri("resource://system/info")
                .name("system-info")
                .title("System information")
                .description("Provides basic system health and metadata")
                .mimeType(APPLICATION_JSON_VALUE)
                .build();

        var systemSpec = new AsyncResourceSpecification(
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
                                            APPLICATION_JSON_VALUE,
                                            objectMapper.writeValueAsString(info)
                                    ))
                            );
                        }).subscribeOn(Schedulers.boundedElastic())
        );

        // user-info (dinámico) — registrado como recurso con URI templada
        var userResource = McpSchema.Resource.builder()
                .uri("resource://users/{userId}")
                .name("user-info")
                .title("User information")
                .description("Retrieve user details by userId")
                .mimeType(APPLICATION_JSON_VALUE)
                .build();

        var userSpec = new AsyncResourceSpecification(
                userResource,
                (McpTransportContext ctx, McpSchema.ReadResourceRequest request) ->
                        Mono.fromCallable(() -> {
                            String userId = request.uri().replace("resource://users/", "");
                            Map<String, Object> user = Map.of(
                                    "userId", userId,
                                    "name", "Usuario " + userId,
                                    "status", "ACTIVE"
                            );
                            return new McpSchema.ReadResourceResult(
                                    List.of(new McpSchema.TextResourceContents(
                                            request.uri(),
                                            APPLICATION_JSON_VALUE,
                                            objectMapper.writeValueAsString(user)
                                    ))
                            );
                        }).subscribeOn(Schedulers.boundedElastic())
        );

        return List.of(systemSpec, userSpec);
    }
}
