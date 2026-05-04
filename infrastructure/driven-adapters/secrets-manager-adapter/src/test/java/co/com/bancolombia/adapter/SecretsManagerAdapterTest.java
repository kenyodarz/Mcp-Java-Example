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

    @Mock
    private SecretsManagerAsyncClient secretsManagerAsyncClient;

    private AsyncSecretsGateway secretsGateway;

    @BeforeEach
    void setUp() {
        secretsGateway = new SecretsManagerAdapter(secretsManagerAsyncClient);
    }

    @Test
    @DisplayName("Debe obtener un secret exitosamente")
    void shouldGetSecretSuccessfully() {
        // Arrange
        String secretName = "test-secret";
        String secretValue = "secret-value";

        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(secretValue)
                .build();

        when(secretsManagerAsyncClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        // Act & Assert
        StepVerifier.create(secretsGateway.getSecret(secretName))
                .expectNext(secretValue)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error cuando no existe el secret")
    void shouldHandleErrorWhenSecretNotFound() {
        // Arrange
        String secretName = "non-existent-secret";
        RuntimeException exception = new RuntimeException("Secret not found");

        CompletableFuture<GetSecretValueResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(exception);

        when(secretsManagerAsyncClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(failedFuture);

        // Act & Assert
        StepVerifier.create(secretsGateway.getSecret(secretName))
                .expectErrorMatches(error -> error instanceof RuntimeException)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar Mono reactivo")
    void shouldReturnReactiveMono() {
        // Arrange
        String secretName = "api-key";
        String secretValue = "key-123456";

        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(secretValue)
                .build();

        when(secretsManagerAsyncClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        // Act
        Mono<String> result = secretsGateway.getSecret(secretName);

        // Assert
        StepVerifier.create(result)
                .expectNext(secretValue)
                .verifyComplete();
    }
}

