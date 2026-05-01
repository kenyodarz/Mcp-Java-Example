package co.com.bancolombia.config;

import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import co.com.bancolombia.usecase.GetCharacterUseCase;
import co.com.bancolombia.usecase.GetEpisodeUseCase;
import co.com.bancolombia.usecase.GetLocationUseCase;
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
    public GetUserInfoUseCase getUserInfoUseCase(UserInfoGateway userInfoGateway) {
        return new GetUserInfoUseCase(userInfoGateway);
    }

    @Bean
    public GetCharacterUseCase getCharacterUseCase(SimpsonsGateway simpsonsGateway) {
        return new GetCharacterUseCase(simpsonsGateway);
    }

    @Bean
    public GetEpisodeUseCase getEpisodeUseCase(SimpsonsGateway simpsonsGateway) {
        return new GetEpisodeUseCase(simpsonsGateway);
    }

    @Bean
    public GetLocationUseCase getLocationUseCase(SimpsonsGateway simpsonsGateway) {
        return new GetLocationUseCase(simpsonsGateway);
    }

    @Bean
    public SaludoUseCase saludoUseCase() {
        return new SaludoUseCase();
    }
}
