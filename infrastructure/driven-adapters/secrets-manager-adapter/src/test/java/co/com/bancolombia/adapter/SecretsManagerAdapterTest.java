package co.com.bancolombia.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecretsManagerAdapter Tests")
class SecretsManagerAdapterTest {

    // ==================== TEST DOUBLES ====================
    // Mock del cliente AWS Secrets Manager para simular respuestas
    @Mock
    private SecretsManagerAsyncClient secretsManagerAsyncClientMock;

    // Sistema bajo prueba (SUT): El adaptador que actúa como gateway para secrets
    private AsyncSecretsGateway secretsGatewaySUT;

    @BeforeEach
    void setUp() {
        // Inicializar el adaptador con el mock del cliente AWS
        secretsGatewaySUT = new SecretsManagerAdapter(secretsManagerAsyncClientMock);
    }

    @Test
    @DisplayName("Debe obtener un secret exitosamente")
    void shouldGetSecretSuccessfully() {
        // ==================== GIVEN ====================
        // Preparar el nombre del secret y el valor esperado
        String secretNameToRetrieve = "test-secret";
        String expectedSecretValue = "secret-value";

        GetSecretValueResponse secretResponseFromAws = GetSecretValueResponse.builder()
                .secretString(expectedSecretValue)
                .build();

        // Configurar el mock para retornar el secret
        when(secretsManagerAsyncClientMock.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(secretResponseFromAws));

        // ==================== WHEN ====================
        // Ejecutar la solicitud para obtener el secret

        // ==================== THEN ====================
        // Verificar que el adaptador retorna el secret value correctamente
        StepVerifier.create(secretsGatewaySUT.getSecret(secretNameToRetrieve))
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

        CompletableFuture<GetSecretValueResponse> failedFutureFromAws = new CompletableFuture<>();
        failedFutureFromAws.completeExceptionally(secretNotFoundExceptionError);

        // Configurar el mock para retornar un error
        when(secretsManagerAsyncClientMock.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(failedFutureFromAws);

        // ==================== WHEN ====================
        // Ejecutar la solicitud para obtener el secret que no existe

        // ==================== THEN ====================
        // Verificar que el error se propaga sin ser capturado
        StepVerifier.create(secretsGatewaySUT.getSecret(nonExistentSecretName))
                .expectErrorMatches(RuntimeException.class::isInstance)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar Mono reactivo")
    void shouldReturnReactiveMono() {
        // ==================== GIVEN ====================
        // Preparar datos para validar que retorna un Mono reactivo
        String secretNameForReactivityTest = "api-key";
        String expectedSecretValueForTest = "key-123456";

        GetSecretValueResponse secretResponseFromAwsForTest = GetSecretValueResponse.builder()
                .secretString(expectedSecretValueForTest)
                .build();

        // Configurar el mock para retornar el secret
        when(secretsManagerAsyncClientMock.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(secretResponseFromAwsForTest));

        // ==================== WHEN ====================
        // Ejecutar la solicitud para obtener el secret y capturar el Mono reactivo
        Mono<String> reactiveResultMono = secretsGatewaySUT.getSecret(secretNameForReactivityTest);

        // ==================== THEN ====================
        // Verificar que el resultado es efectivamente un Mono reactivo
        StepVerifier.create(reactiveResultMono)
                .expectNext(expectedSecretValueForTest)
                .verifyComplete();
    }
}

