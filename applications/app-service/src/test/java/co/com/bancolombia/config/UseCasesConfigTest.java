package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import co.com.bancolombia.model.userinfo.gateways.UserInfoGateway;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public UserInfoGateway userInfoGateway() {
            return id -> Mono.empty();
        }

        @Bean
        public AsyncSecretsGateway asyncSecretsGateway() {
            return secretName -> Mono.just("mock-secret-value");
        }

        @Bean
        public SimpsonsGateway simpsonsGateway() {
            return new SimpsonsGateway() {
                @Override
                public Mono<co.com.bancolombia.model.simpsons.SimpsonsCharacter> getCharacterById(
                        Integer id) {
                    return Mono.empty();
                }

                @Override
                public Mono<co.com.bancolombia.model.simpsons.SimpsonsEpisode> getEpisodeById(
                        Integer id) {
                    return Mono.empty();
                }

                @Override
                public Mono<co.com.bancolombia.model.simpsons.SimpsonsLocation> getLocationById(
                        Integer id) {
                    return Mono.empty();
                }
            };
        }

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}

