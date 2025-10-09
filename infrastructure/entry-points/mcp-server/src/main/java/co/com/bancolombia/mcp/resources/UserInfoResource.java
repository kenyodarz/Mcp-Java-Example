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
public class UserInfoResource {

    private final ObjectMapper objectMapper;

    public UserInfoResource(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AsyncResourceSpecification getResourceSpecification() {
        var userResource = McpSchema.Resource.builder()
                .uri("resource://users/{userId}")
                .name("user-info")
                .title("User information")
                .description("Retrieve user details by userId")
                .mimeType(MediaType.APPLICATION_JSON_VALUE)
                .build();

        return new AsyncResourceSpecification(
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
                                            MediaType.APPLICATION_JSON_VALUE,
                                            objectMapper.writeValueAsString(user)
                                    ))
                            );
                        }).subscribeOn(Schedulers.boundedElastic())
        );
    }
}