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

    // ==================== TEST DOUBLES ====================
    // Mock del gateway que proporciona acceso a los secrets
    @Mock
    private AsyncSecretsGateway secretsGatewayMock;

    // Sistema bajo prueba (SUT): Caso de uso para obtener secretos MCP
    private GetMcpSecretsUseCase getMcpSecretsUseCaseSUT;

    @BeforeEach
    void setUp() {
        // Inicializar el caso de uso con el mock del gateway
        getMcpSecretsUseCaseSUT = new GetMcpSecretsUseCase(secretsGatewayMock);
    }

    @Test
    @DisplayName("Debe obtener credenciales MCP exitosamente")
    void shouldGetMcpSecretsSuccessfully() {
        // ==================== GIVEN ====================
        // Preparar credenciales MCP esperadas
        String expectedClientId = "mcp-client-123";
        String expectedClientSecret = "mcp-secret-456";

        // ==================== WHEN ====================
        // Configurar el mock para retornar las credenciales
        when(secretsGatewayMock.getSecret("mcp-client-id"))
                .thenReturn(Mono.just(expectedClientId));

        when(secretsGatewayMock.getSecret("mcp-client-secret"))
                .thenReturn(Mono.just(expectedClientSecret));

        // ==================== THEN ====================
        // Verificar que el caso de uso retorna las credenciales en un Map
        StepVerifier.create(getMcpSecretsUseCaseSUT.execute())
                .expectNextMatches(credentialsMap ->
                        credentialsMap.get("clientId").equals(expectedClientId) &&
                                credentialsMap.get("clientSecret").equals(expectedClientSecret)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error cuando no existe algún secret")
    void shouldHandleErrorWhenSecretNotFound() {
        // ==================== GIVEN ====================
        // Preparar un escenario donde uno de los secrets no existe
        RuntimeException secretNotFoundExceptionError = new RuntimeException("Secret not found");

        // ==================== WHEN ====================
        // Configurar el mock para retornar error en el segundo secret
        when(secretsGatewayMock.getSecret("mcp-client-id"))
                .thenReturn(Mono.just("client-id"));

        when(secretsGatewayMock.getSecret("mcp-client-secret"))
                .thenReturn(Mono.error(secretNotFoundExceptionError));

        // ==================== THEN ====================
        // Verificar que el error se propaga sin ser capturado
        StepVerifier.create(getMcpSecretsUseCaseSUT.execute())
                .expectErrorMessage("Secret not found")
                .verify();
    }

    @Test
    @DisplayName("Debe retornar Mono reactivo con Map")
    void shouldReturnReactiveMonoWithMap() {
        // ==================== GIVEN ====================
        // Preparar credenciales MCP para validar que retorna el tipo correcto
        String expectedClientId = "client-789";
        String expectedClientSecret = "secret-101112";

        // ==================== WHEN ====================
        // Configurar el mock para retornar las credenciales
        when(secretsGatewayMock.getSecret("mcp-client-id"))
                .thenReturn(Mono.just(expectedClientId));

        when(secretsGatewayMock.getSecret("mcp-client-secret"))
                .thenReturn(Mono.just(expectedClientSecret));

        // ==================== THEN ====================
        // Verificar que el resultado es un Mono reactivo con Map que contiene las claves esperadas
        Mono<Map<String, String>> reactiveResultMono = getMcpSecretsUseCaseSUT.execute();

        StepVerifier.create(reactiveResultMono)
                .expectNextMatches(credentialsMap ->
                        credentialsMap.containsKey("clientId") &&
                                credentialsMap.containsKey("clientSecret")
                )
                .verifyComplete();
    }
}

