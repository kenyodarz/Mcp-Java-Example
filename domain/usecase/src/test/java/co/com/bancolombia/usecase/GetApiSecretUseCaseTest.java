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

    @Mock
    private AsyncSecretsGateway secretsGateway;

    private GetApiSecretUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetApiSecretUseCase(secretsGateway);
    }

    @Test
    @DisplayName("Debe obtener un secret de API exitosamente")
    void shouldGetApiSecretSuccessfully() {
        // Arrange
        String secretName = "api-consumer-key";
        String secretValue = "consumer-key-12345";

        when(secretsGateway.getSecret(secretName))
                .thenReturn(Mono.just(secretValue));

        // Act & Assert
        StepVerifier.create(useCase.execute(secretName))
                .expectNext(secretValue)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error cuando no existe el secret")
    void shouldHandleErrorWhenSecretNotFound() {
        // Arrange
        String secretName = "non-existent-secret";
        RuntimeException exception = new RuntimeException("Secret not found");

        when(secretsGateway.getSecret(secretName))
                .thenReturn(Mono.error(exception));

        // Act & Assert
        StepVerifier.create(useCase.execute(secretName))
                .expectErrorMessage("Secret not found")
                .verify();
    }

    @Test
    @DisplayName("Debe retornar Mono reactivo")
    void shouldReturnReactiveMono() {
        // Arrange
        String secretName = "api-key";
        String secretValue = "key-123456";

        when(secretsGateway.getSecret(secretName))
                .thenReturn(Mono.just(secretValue));

        // Act
        Mono<String> result = useCase.execute(secretName);

        // Assert
        StepVerifier.create(result)
                .expectNext(secretValue)
                .verifyComplete();
    }
}

