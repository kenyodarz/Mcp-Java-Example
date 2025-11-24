package co.com.bancolombia.config;

import co.com.bancolombia.model.apikey.gateways.ApiKeyGateway;
import co.com.bancolombia.model.apikey.gateways.PasswordEncoderGateway;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import co.com.bancolombia.usecase.GetUserInfoUseCase;
import co.com.bancolombia.usecase.apikey.RotateExpiredApiKeysUseCase;
import co.com.bancolombia.usecase.apikey.ValidateApiKeyUseCase;
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
    public GetUserInfoUseCase getUserInfoUseCase(UserInfoGateway userInfoGateway) {
        return new GetUserInfoUseCase(userInfoGateway);
    }

    @Bean
    public ValidateApiKeyUseCase validateApiKeyUseCase(
            ApiKeyGateway apiKeyGateway,
            PasswordEncoderGateway passwordEncoderGateway
    ) {
        return new ValidateApiKeyUseCase(apiKeyGateway, passwordEncoderGateway);
    }

    @Bean
    public RotateExpiredApiKeysUseCase rotateExpiredApiKeysUseCase(
            ApiKeyGateway apiKeyGateway
    ) {
        return new RotateExpiredApiKeysUseCase(apiKeyGateway);
    }
}
