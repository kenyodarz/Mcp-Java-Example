package co.com.bancolombia.usecase;

import static org.mockito.Mockito.when;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetApiSecretUseCase Tests")
class GetApiSecretUseCaseTest {

    // ==================== TEST DOUBLES ====================
    // Mock del gateway que proporciona acceso a los secrets
    @Mock
    private AsyncSecretsGateway secretsGatewayMock;

    // Sistema bajo prueba (SUT): Caso de uso para obtener secrets de API
    private GetApiSecretUseCase getApiSecretUseCaseSUT;

    @BeforeEach
    void setUp() {
        // Inicializar el caso de uso con el mock del gateway
        getApiSecretUseCaseSUT = new GetApiSecretUseCase(secretsGatewayMock);
    }

    @Test
    @DisplayName("Debe obtener un secret de API exitosamente")
    void shouldGetApiSecretSuccessfully() {
        // ==================== GIVEN ====================
        // Preparar el nombre del secret y el valor esperado
        String secretNameToRequest = "api-consumer-key";
        String expectedSecretValue = "consumer-key-12345";

        // ==================== WHEN ====================
        // Configurar el mock para retornar el secret
        when(secretsGatewayMock.getSecret(secretNameToRequest))
                .thenReturn(Mono.just(expectedSecretValue));

        // ==================== THEN ====================
        // Verificar que el caso de uso retorna el secret esperado
        StepVerifier.create(getApiSecretUseCaseSUT.execute(secretNameToRequest))
                .expectNext(expectedSecretValue)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error cuando no existe el secret")
    void shouldHandleErrorWhenSecretNotFound() {
        // ==================== GIVEN ====================
        // Preparar un escenario donde el secret no existe
        String nonExistentSecretName = "non-existent-secret";
        RuntimeException secretNotFoundExceptionError = new RuntimeException("Secret not found");

        // ==================== WHEN ====================
        // Configurar el mock para retornar un error
        when(secretsGatewayMock.getSecret(nonExistentSecretName))
                .thenReturn(Mono.error(secretNotFoundExceptionError));

        // ==================== THEN ====================
        // Verificar que el error se propaga sin ser capturado
        StepVerifier.create(getApiSecretUseCaseSUT.execute(nonExistentSecretName))
                .expectErrorMessage("Secret not found")
                .verify();
    }

    @Test
    @DisplayName("Debe retornar Mono reactivo")
    void shouldReturnReactiveMono() {
        // ==================== GIVEN ====================
        // Preparar el nombre del secret y el valor esperado para validar reactividad
        String secretNameToRequest = "api-key";
        String expectedSecretValue = "key-123456";

        // ==================== WHEN ====================
        // Configurar el mock para retornar el secret
        when(secretsGatewayMock.getSecret(secretNameToRequest))
                .thenReturn(Mono.just(expectedSecretValue));

        // ==================== THEN ====================
        // Verificar que el resultado es un Mono reactivo
        Mono<String> reactiveResultMono = getApiSecretUseCaseSUT.execute(secretNameToRequest);

        StepVerifier.create(reactiveResultMono)
                .expectNext(expectedSecretValue)
                .verifyComplete();
    }
}

