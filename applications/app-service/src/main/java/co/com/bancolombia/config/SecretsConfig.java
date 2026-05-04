package co.com.bancolombia.config;

import co.com.bancolombia.adapter.SecretsManagerAdapter;
import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;

/**
 * Configuración de AWS Secrets Manager Async Client y SecretsManagerAdapter.
 * <p>
 * Se adapta automáticamente a Ministack (dev) o AWS real (prod) según:
 * aws.secretsmanager.endpoint y aws.region.
 */
@Slf4j
@Configuration
public class SecretsConfig {

    /**
     * Bean: SecretsManagerAsyncClient Se configura para ministack o AWS según el ambiente.
     */
    @Bean
    public SecretsManagerAsyncClient secretsManagerAsyncClient(
            @Value("${aws.region:us-east-1}") final String region,
            @Value("${aws.secretsmanager.endpoint:}") final String endpoint) {
        final boolean useEndpointOverride = endpoint != null && !endpoint.isBlank();
        log.info("Configurando SecretsManagerAsyncClient - endpointOverride: {}, region: {}",
                useEndpointOverride, region);

        SecretsManagerAsyncClientBuilder builder = SecretsManagerAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (useEndpointOverride) {
            log.info("Usando endpoint de Secrets Manager: {}", endpoint);
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * Bean: AsyncSecretsGateway Inyectable en UseCases y Driven Adapters.
     */
    @Bean
    public AsyncSecretsGateway asyncSecretsGateway(final SecretsManagerAsyncClient client) {
        log.info("Creando bean AsyncSecretsGateway (SecretsManagerAdapter)");
        return new SecretsManagerAdapter(client);
    }
}

