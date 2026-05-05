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

	@Mock
	private UserInfoGateway userInfoGateway;

	private GetUserInfoUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new GetUserInfoUseCase(userInfoGateway);
	}

	@Test
	void shouldDelegateUserLookupToGateway() {
		UserInfo userInfo = UserInfo.builder()
				.id(1)
				.name("Homer Simpson")
				.occupation("Safety Inspector")
				.phrases(List.of("Doh!"))
				.build();

		when(userInfoGateway.getUserInfoById(1)).thenReturn(Mono.just(userInfo));

		StepVerifier.create(useCase.execute(1))
				.expectNext(userInfo)
				.verifyComplete();

		verify(userInfoGateway).getUserInfoById(1);
	}

	@Test
	void shouldPropagateGatewayErrors() {
		when(userInfoGateway.getUserInfoById(99)).thenReturn(
				Mono.error(new IllegalStateException("boom")));

		StepVerifier.create(useCase.execute(99))
				.expectErrorMessage("boom")
				.verify();

		verify(userInfoGateway).getUserInfoById(99);
	}
}