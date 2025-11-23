package co.com.bancolombia.mcp.resources;

import co.com.bancolombia.mcp.response.ErrorResponse;
import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.usecase.GetUserInfoUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

/**
 * Resource para obtener información de usuarios Implementa caché y manejo de errores mejorado
 */
@Slf4j
@Component
public class UserInfoResource {

    private final ObjectMapper objectMapper;
    private final GetUserInfoUseCase getUserInfoUseCase;

    public UserInfoResource(ObjectMapper objectMapper, GetUserInfoUseCase getUserInfoUseCase) {
        this.objectMapper = objectMapper;
        this.getUserInfoUseCase = getUserInfoUseCase;
        log.info("UserInfoResource inicializado correctamente");
    }

    public AsyncResourceTemplateSpecification getResourceSpecification() {
        log.debug("Creando especificación para UserInfoResource");

        // Definición del template del resource
        McpSchema.ResourceTemplate userResource = McpSchema.ResourceTemplate.builder()
                .uriTemplate("resource://users/{userId}")
                .name("user-info")
                .title("Información del Usuario")
                .description(
                        "Obtiene información detallada de un usuario por su ID desde la API de Simpsons")
                .mimeType(MediaType.APPLICATION_JSON_VALUE)
                .build();

        return new AsyncResourceTemplateSpecification(
                userResource,
                (McpTransportContext ctx, McpSchema.ReadResourceRequest request) -> {
                    log.info("Solicitud de recurso recibida: {}", request.uri());

                    return extractUserId(request.uri())
                            .flatMap(this::fetchUserInfo)
                            .map(userInfo -> createResourceResult(request.uri(), userInfo))
                            .timeout(Duration.ofSeconds(10))
                            .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                                    .maxBackoff(Duration.ofSeconds(2))
                                    .filter(throwable -> !(throwable instanceof IllegalArgumentException))
                                    .doBeforeRetry(retrySignal ->
                                            log.warn(
                                                    "Reintentando obtener información del usuario, intento: {}",
                                                    retrySignal.totalRetries() + 1))
                            )
                            .onErrorResume(this::handleError)
                            .subscribeOn(Schedulers.boundedElastic());
                }
        );
    }

    /**
     * Extrae el userId de la URI del request
     */
    private Mono<Integer> extractUserId(String uri) {
        return Mono.fromCallable(() -> {
            try {
                String userIdStr = uri.replace("resource://users/", "").trim();

                if (userIdStr.isEmpty()) {
                    throw new IllegalArgumentException("El userId no puede estar vacío");
                }

                int userId = Integer.parseInt(userIdStr);

                if (userId <= 0) {
                    throw new IllegalArgumentException("El userId debe ser un número positivo");
                }

                log.debug("UserId extraído exitosamente: {}", userId);
                return userId;

            } catch (NumberFormatException e) {
                String errorMsg = "El userId debe ser un número válido";
                log.error(errorMsg, e);
                throw new IllegalArgumentException(errorMsg, e);
            }
        });
    }

    /**
     * Obtiene la información del usuario usando el use case
     */
    private Mono<UserInfo> fetchUserInfo(Integer userId) {
        log.debug("Obteniendo información del usuario: {}", userId);

        return getUserInfoUseCase.execute(userId)
                .doOnSuccess(
                        userInfo -> log.info("Información del usuario {} obtenida exitosamente",
                                userId))
                .doOnError(error -> log.error("Error al obtener información del usuario {}", userId,
                        error))
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Usuario no encontrado con ID: " + userId))
                );
    }

    /**
     * Crea el resultado del resource
     */
    private McpSchema.ReadResourceResult createResourceResult(String uri, UserInfo userInfo) {
        try {
            String jsonContent = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(userInfo);

            McpSchema.TextResourceContents content = new McpSchema.TextResourceContents(
                    uri,
                    MediaType.APPLICATION_JSON_VALUE,
                    jsonContent
            );

            log.debug("Resultado del recurso creado exitosamente para URI: {}", uri);
            return new McpSchema.ReadResourceResult(List.of(content));

        } catch (JsonProcessingException e) {
            log.error("Error al serializar UserInfo a JSON", e);
            throw new RuntimeException("Error al crear respuesta JSON", e);
        }
    }

    /**
     * Maneja los errores de forma consistente
     */
    private Mono<McpSchema.ReadResourceResult> handleError(Throwable error) {
        log.error("Error al procesar el recurso de usuario", error);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(true)
                .message(error.getMessage())
                .type(error.getClass().getSimpleName())
                .build();

        try {
            String errorJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(errorResponse);

            McpSchema.TextResourceContents errorContent = new McpSchema.TextResourceContents(
                    "resource://error",
                    MediaType.APPLICATION_JSON_VALUE,
                    errorJson
            );

            return Mono.just(new McpSchema.ReadResourceResult(List.of(errorContent)));

        } catch (JsonProcessingException e) {
            log.error("Error crítico al crear respuesta de error", e);
            return Mono.error(e);
        }
    }
}