package co.com.bancolombia.mcp.resources;

import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.usecase.GetUserInfoUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UserInfoResourceTest {

    // ==================== TEST DOUBLES ====================
    // Mapper para serializar/deserializar JSON
    private final JsonMapper jsonMapperDouble = JsonMapper.builder().build();

    // Mock del caso de uso para obtener información de usuarios
    private final GetUserInfoUseCase getUserInfoUseCaseMock = Mockito.mock(
            GetUserInfoUseCase.class);

    // Sistema bajo prueba (SUT): El recurso MCP que expone la información de usuarios
    private final UserInfoResource userInfoResourceSUT =
            new UserInfoResource(jsonMapperDouble, getUserInfoUseCaseMock);

    private UserInfo buildExpectedHomerSimpsonsUserInfo() {
        return UserInfo.builder()
                .id(1)
                .age(39)
                .birthdate("1956-05-12")
                .name("Homer Simpson")
                .gender("Male")
                .occupation("Safety Inspector")
                .portraitPath("/character/1.webp")
                .status("Alive")
                .phrases(List.of("Doh!", "Woo-hoo!"))
                .build();
    }


    @Test
    @DisplayName("Debe retornar información del usuario en JSON usando ReadResourceResult")
    void shouldReturnUserInfoSuccessfully() {
        // ==================== GIVEN ====================
        // Preparar la información del usuario esperada
        UserInfo expectedUserInfo = buildExpectedHomerSimpsonsUserInfo();

        // Configurar el mock del caso de uso
        Mockito.when(getUserInfoUseCaseMock.execute(1)).thenReturn(Mono.just(expectedUserInfo));

        // ==================== WHEN ====================
        // Ejecutar la solicitud del recurso para obtener la información del usuario
        var userInfoResourceResultMono = userInfoResourceSUT.getUserInfo("1");

        // ==================== THEN ====================
        // Verificar que el recurso retorna información válida en formato JSON
        StepVerifier.create(userInfoResourceResultMono)
                .assertNext(readResourceResult -> {

                    List<ResourceContents> resourceContentsList = readResourceResult.contents();
                    assert resourceContentsList.size() == 1;

                    ResourceContents resourceContent = resourceContentsList.getFirst();
                    assert resourceContent instanceof TextResourceContents;

                    TextResourceContents textResourceContent = (TextResourceContents) resourceContent;

                    assert textResourceContent.uri().equals("resource://users/1");
                    assert textResourceContent.mimeType().equals("application/json");

                    // Validar que el JSON es válido
                    JsonNode jsonNode;
                    try {
                        jsonNode = jsonMapperDouble.readTree(textResourceContent.text());
                    } catch (JsonProcessingException jsonEx) {
                        throw new RuntimeException(jsonEx);
                    }

                    // Validar SOLO campos obligatorios del modelo UserInfo
                    assert jsonNode.get("id").asInt() == 1;
                    assert jsonNode.get("name").asText().equals("Homer Simpson");
                    assert jsonNode.get("gender").asText().equals("Male");
                    assert jsonNode.get("occupation").asText().equals("Safety Inspector");
                    assert jsonNode.get("status").asText().equals("Alive");

                    // Validar que 'phrases' existe
                    assert jsonNode.get("phrases").isArray();

                })
                .verifyComplete();

        // Verificar que el caso de uso fue invocado
        Mockito.verify(getUserInfoUseCaseMock).execute(1);
    }


    @Test
    @DisplayName("Debe retornar error cuando el ID es inválido")
    void shouldReturnErrorForInvalidUserId() {
        // ==================== GIVEN ====================
        // Preparar un ID de usuario inválido (no es un número)
        String invalidUserIdParameter = "abc";

        // ==================== WHEN ====================
        // Ejecutar la solicitud del recurso con un ID inválido
        var userInfoResourceResultMono = userInfoResourceSUT.getUserInfo(invalidUserIdParameter);

        // ==================== THEN ====================
        // Verificar que el recurso retorna un error JSON
        StepVerifier.create(userInfoResourceResultMono)
                .assertNext(readResourceResult -> {
                    ResourceContents resourceContent = readResourceResult.contents().getFirst();
                    assert resourceContent instanceof TextResourceContents;

                    TextResourceContents textResourceContent = (TextResourceContents) resourceContent;

                    assert textResourceContent.uri().equals("resource://users/abc");
                    assert textResourceContent.mimeType().equals("application/json");

                    String errorJsonResponse = textResourceContent.text();
                    assert errorJsonResponse.contains("error");
                    assert errorJsonResponse.contains("userId");
                    assert errorJsonResponse.contains("IllegalArgumentException");
                })
                .verifyComplete();

        // Verificar que el caso de uso NO fue invocado (error detectado antes)
        Mockito.verifyNoInteractions(getUserInfoUseCaseMock);
    }
}
