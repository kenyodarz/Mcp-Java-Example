package co.com.bancolombia.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.bancolombia.model.userinfo.UserInfo;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetUserInfoUseCaseTest {

	// ==================== TEST DOUBLES ====================
	// Mock del gateway que proporciona información de usuarios del adaptador
	@Mock
	private UserInfoGateway userInfoGatewayMock;

	// Sistema bajo prueba (SUT): El caso de uso que ejecuta la lógica de negocio
	private GetUserInfoUseCase getUserInfoUseCaseSUT;

	@BeforeEach
	void setUp() {
		// Inicializar el caso de uso con el mock del gateway
		getUserInfoUseCaseSUT = new GetUserInfoUseCase(userInfoGatewayMock);
	}

	@Test
	void shouldDelegateUserLookupToGateway() {
		// ==================== GIVEN ====================
		// Preparar datos de usuario esperados del gateway
		UserInfo expectedUserInfo = UserInfo.builder()
				.id(1)
				.name("Homer Simpson")
				.occupation("Safety Inspector")
				.phrases(List.of("Doh!"))
				.build();

		// ==================== WHEN ====================
		// Configurar el mock para retornar la información de usuario
		when(userInfoGatewayMock.getUserInfoById(1)).thenReturn(Mono.just(expectedUserInfo));

		// ==================== THEN ====================
		// Verificar que el caso de uso delega la búsqueda al gateway correctamente
		StepVerifier.create(getUserInfoUseCaseSUT.execute(1))
				.expectNext(expectedUserInfo)
				.verifyComplete();

		// Verificar que el gateway fue invocado con el ID correcto
		verify(userInfoGatewayMock).getUserInfoById(1);
	}

	@Test
	void shouldPropagateGatewayErrors() {
		// ==================== GIVEN ====================
		// Preparar un escenario donde el gateway genera un error
		IllegalStateException gatewayFailureException = new IllegalStateException("boom");

		// ==================== WHEN ====================
		// Configurar el mock para retornar un error
		when(userInfoGatewayMock.getUserInfoById(99)).thenReturn(
				Mono.error(gatewayFailureException));

		// ==================== THEN ====================
		// Verificar que el error del gateway se propaga sin ser capturado
		StepVerifier.create(getUserInfoUseCaseSUT.execute(99))
				.expectErrorMessage("boom")
				.verify();

		// Verificar que el gateway fue invocado
		verify(userInfoGatewayMock).getUserInfoById(99);
	}
}