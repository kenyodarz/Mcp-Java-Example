package co.com.bancolombia.config;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import co.com.bancolombia.usecase.GetApiSecretUseCase;
import co.com.bancolombia.usecase.GetCharacterUseCase;
import co.com.bancolombia.usecase.GetEpisodeUseCase;
import co.com.bancolombia.usecase.GetLocationUseCase;
import co.com.bancolombia.usecase.GetMcpSecretsUseCase;
import co.com.bancolombia.usecase.GetUserInfoUseCase;
import co.com.bancolombia.usecase.SaludoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.bancolombia.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    @Bean
    public GetUserInfoUseCase getUserInfoUseCase(final UserInfoGateway userInfoGateway) {
        return new GetUserInfoUseCase(userInfoGateway);
    }

    @Bean
    public GetCharacterUseCase getCharacterUseCase(final SimpsonsGateway simpsonsGateway) {
        return new GetCharacterUseCase(simpsonsGateway);
    }

    @Bean
    public GetEpisodeUseCase getEpisodeUseCase(final SimpsonsGateway simpsonsGateway) {
        return new GetEpisodeUseCase(simpsonsGateway);
    }

    @Bean
    public GetLocationUseCase getLocationUseCase(final SimpsonsGateway simpsonsGateway) {
        return new GetLocationUseCase(simpsonsGateway);
    }

    @Bean
    public SaludoUseCase saludoUseCase() {
        return new SaludoUseCase();
    }

    // ============================================
    // SECRETS MANAGER USE CASES
    // ============================================

    /**
     * UseCase para obtener secretos de APIs externas.
     */
    @Bean
    public GetApiSecretUseCase getApiSecretUseCase(final AsyncSecretsGateway secretsGateway) {
        return new GetApiSecretUseCase(secretsGateway);
    }

    /**
     * UseCase para obtener credenciales del MCP.
     */
    @Bean
    public GetMcpSecretsUseCase getMcpSecretsUseCase(final AsyncSecretsGateway secretsGateway) {
        return new GetMcpSecretsUseCase(secretsGateway);
    }
}
