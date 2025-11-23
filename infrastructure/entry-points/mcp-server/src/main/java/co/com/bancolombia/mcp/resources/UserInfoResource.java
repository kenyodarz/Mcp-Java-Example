package co.com.bancolombia.mcp.resources;

import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.usecase.GetUserInfoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Resource de información de usuario usando anotaciones MCP con template URI
 * <p>
 * Con @McpResource y {userId}, Spring AI automáticamente: - Registra el resource template - Extrae
 * el parámetro de la URI - Inyecta el parámetro en el metodo
 * <p>
 * IMPORTANTE: Para ASYNC server, el metodo debe retornar Mono<ReadResourceResult>
 */
@Slf4j
@Component
public class UserInfoResource {

    private final ObjectMapper objectMapper;
    private final GetUserInfoUseCase getUserInfoUseCase;

    public UserInfoResource(ObjectMapper objectMapper, GetUserInfoUseCase getUserInfoUseCase) {
        this.objectMapper = objectMapper;
        this.getUserInfoUseCase = getUserInfoUseCase;
    }

    @McpResource(
            uri = "resource://users/{userId}",
            name = "user-info",
            description = "Obtiene información detallada de un usuario por su ID desde la API de Simpsons"
    )
    public Mono<ReadResourceResult> getUserInfo(String userId) {
        log.info("📥 Solicitud de información para userId: {}", userId);

        return parseUserId(userId)
                .flatMap(id -> {
                    log.debug("🔍 Obteniendo información del usuario: {}", id);
                    return getUserInfoUseCase.execute(id);
                })
                .map(userInfo -> {
                    log.info("✅ Información del usuario {} obtenida exitosamente", userId);
                    return createResourceResult(userId, userInfo);
                })
                .onErrorResume(error -> {
                    log.error("❌ Error al obtener información del usuario {}: {}", userId,
                            error.getMessage());
                    return Mono.just(createErrorResult(userId, error));
                });
    }

    /**
     * Parsea el userId desde String a Integer
     */
    private Mono<Integer> parseUserId(String userIdStr) {
        return Mono.fromCallable(() -> {
            try {
                int userId = Integer.parseInt(userIdStr);
                if (userId <= 0) {
                    throw new IllegalArgumentException("El userId debe ser un número positivo");
                }
                return userId;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "El userId debe ser un número válido: " + userIdStr);
            }
        });
    }

    /**
     * Crea el resultado exitoso del resource
     */
    private ReadResourceResult createResourceResult(String userId, UserInfo userInfo) {
        return new ReadResourceResult(
                List.of(new TextResourceContents(
                        "resource://users/" + userId,
                        MediaType.APPLICATION_JSON_VALUE,
                        toJson(userInfo)
                ))
        );
    }

    /**
     * Crea el resultado de error
     */
    private ReadResourceResult createErrorResult(String userId, Throwable error) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(true)
                .message(error.getMessage())
                .type(error.getClass().getSimpleName())
                .userId(userId)
                .build();

        return new ReadResourceResult(
                List.of(new TextResourceContents(
                        "resource://users/" + userId,
                        MediaType.APPLICATION_JSON_VALUE,
                        toJson(errorResponse)
                ))
        );
    }

    @SneakyThrows
    private String toJson(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    /**
     * Clase para representar errores
     */
    @lombok.Data
    @lombok.Builder
    private static class ErrorResponse {

        private boolean error;
        private String message;
        private String type;
        private String userId;
    }
}