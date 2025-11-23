package co.com.bancolombia.mcp.resources;

import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.usecase.GetUserInfoUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UserInfoResourceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final GetUserInfoUseCase useCase = Mockito.mock(GetUserInfoUseCase.class);
    private final UserInfoResource resource =
            new UserInfoResource(mapper, useCase);

    private UserInfo buildUserInfo() {
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

        // Given: el caso de uso retorna UserInfo
        UserInfo userInfo = buildUserInfo();
        Mockito.when(useCase.execute(1)).thenReturn(Mono.just(userInfo));

        // When
        var resultMono = resource.getUserInfo("1");

        // Then
        StepVerifier.create(resultMono)
                .assertNext(result -> {

                    List<ResourceContents> contents = result.contents();
                    assert contents.size() == 1;

                    ResourceContents content = contents.getFirst();
                    assert content instanceof TextResourceContents;

                    TextResourceContents text = (TextResourceContents) content;

                    assert text.uri().equals("resource://users/1");
                    assert text.mimeType().equals("application/json");

                    // Validamos JSON válido
                    JsonNode jsonNode;
                    try {
                        jsonNode = mapper.readTree(text.text());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    // Validamos SOLO campos obligatorios del modelo UserInfo
                    assert jsonNode.get("id").asInt() == 1;
                    assert jsonNode.get("name").asText().equals("Homer Simpson");
                    assert jsonNode.get("gender").asText().equals("Male");
                    assert jsonNode.get("occupation").asText().equals("Safety Inspector");
                    assert jsonNode.get("status").asText().equals("Alive");

                    // Validamos que 'phrases' exista
                    assert jsonNode.get("phrases").isArray();

                })
                .verifyComplete();

        Mockito.verify(useCase).execute(1);
    }


    @Test
    @DisplayName("Debe retornar error cuando el ID es inválido")
    void shouldReturnErrorForInvalidUserId() {

        // When
        var resultMono = resource.getUserInfo("abc");

        // Then
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    ResourceContents content = result.contents().getFirst();
                    assert content instanceof TextResourceContents;

                    TextResourceContents text = (TextResourceContents) content;

                    assert text.uri().equals("resource://users/abc");
                    assert text.mimeType().equals("application/json");

                    String json = text.text();
                    assert json.contains("error");
                    assert json.contains("userId");
                    assert json.contains("IllegalArgumentException");
                })
                .verifyComplete();

        Mockito.verifyNoInteractions(useCase);
    }
}
