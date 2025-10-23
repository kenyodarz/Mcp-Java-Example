package co.com.bancolombia.mcp.resources;

import co.com.bancolombia.usecase.GetUserInfoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class UserInfoResource {

    private final ObjectMapper objectMapper;
    private final GetUserInfoUseCase getUserInfoUseCase;

    public UserInfoResource(ObjectMapper objectMapper, GetUserInfoUseCase getUserInfoUseCase) {
        this.objectMapper = objectMapper;
        this.getUserInfoUseCase = getUserInfoUseCase;
    }

    public AsyncResourceTemplateSpecification getResourceSpecification() {
        // ✅ Ahora usamos ResourceTemplate en lugar de Resource
        var userResource = McpSchema.ResourceTemplate.builder()
                .uriTemplate("resource://users/{userId}")
                .name("user-info")
                .title("User information")
                .description("Retrieve user details by userId")
                .mimeType(MediaType.APPLICATION_JSON_VALUE)
                .build();

        return new AsyncResourceTemplateSpecification(
                userResource,
                (McpTransportContext ctx, McpSchema.ReadResourceRequest request) -> {
                    String userIdStr = request.uri().replace("resource://users/", "").trim();

                    int userId;
                    try {
                        userId = Integer.parseInt(userIdStr);
                    } catch (NumberFormatException e) {
                        return Mono.error(new IllegalArgumentException(
                                "Invalid userId in URI: " + userIdStr));
                    }

                    // ✅ El flujo sigue siendo totalmente reactivo
                    return getUserInfoUseCase.execute(userId)
                            .map(userInfo -> new McpSchema.ReadResourceResult(
                                    List.of(new McpSchema.TextResourceContents(
                                            request.uri(),
                                            MediaType.APPLICATION_JSON_VALUE,
                                            toJson(userInfo)
                                    ))
                            ))
                            .subscribeOn(Schedulers.boundedElastic());
                }
        );
    }

    @SneakyThrows
    private String toJson(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
