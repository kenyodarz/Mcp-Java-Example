package co.com.bancolombia.usecase;

import static org.mockito.Mockito.when;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMcpSecretsUseCase Tests")
class GetMcpSecretsUseCaseTest {

    @Mock
    private AsyncSecretsGateway secretsGateway;

    private GetMcpSecretsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMcpSecretsUseCase(secretsGateway);
    }

    @Test
    @DisplayName("Debe obtener credenciales MCP exitosamente")
    void shouldGetMcpSecretsSuccessfully() {
        // Arrange
        String clientId = "mcp-client-123";
        String clientSecret = "mcp-secret-456";

        when(secretsGateway.getSecret("mcp-client-id"))
                .thenReturn(Mono.just(clientId));

        when(secretsGateway.getSecret("mcp-client-secret"))
                .thenReturn(Mono.just(clientSecret));

        // Act & Assert
        StepVerifier.create(useCase.execute())
                .expectNextMatches(creds ->
                        creds.get("clientId").equals(clientId) &&
                                creds.get("clientSecret").equals(clientSecret)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error cuando no existe algún secret")
    void shouldHandleErrorWhenSecretNotFound() {
        // Arrange
        RuntimeException exception = new RuntimeException("Secret not found");

        when(secretsGateway.getSecret("mcp-client-id"))
                .thenReturn(Mono.just("client-id"));

        when(secretsGateway.getSecret("mcp-client-secret"))
                .thenReturn(Mono.error(exception));

        // Act & Assert
        StepVerifier.create(useCase.execute())
                .expectErrorMessage("Secret not found")
                .verify();
    }

    @Test
    @DisplayName("Debe retornar Mono reactivo con Map")
    void shouldReturnReactiveMonoWithMap() {
        // Arrange
        String clientId = "client-789";
        String clientSecret = "secret-101112";

        when(secretsGateway.getSecret("mcp-client-id"))
                .thenReturn(Mono.just(clientId));

        when(secretsGateway.getSecret("mcp-client-secret"))
                .thenReturn(Mono.just(clientSecret));

        // Act
        Mono<Map<String, String>> result = useCase.execute();

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(creds ->
                        creds.containsKey("clientId") &&
                                creds.containsKey("clientSecret")
                )
                .verifyComplete();
    }
}

