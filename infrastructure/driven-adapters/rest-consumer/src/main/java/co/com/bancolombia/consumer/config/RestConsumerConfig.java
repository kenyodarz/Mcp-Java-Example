package co.com.bancolombia.consumer.config;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import co.com.bancolombia.consumer.config.properties.RestConsumerProperties;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class RestConsumerConfig {

    private final RestConsumerProperties properties;

    public RestConsumerConfig(RestConsumerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebClient getWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(properties.getUrl())
                .defaultHeaders(this::applyDefaultHeaders)
                .clientConnector(getClientHttpConnector())
                .build();
    }

    private void applyDefaultHeaders(HttpHeaders headers) {
        properties.getHeaders().forEach(headers::add);
    }

    private ClientHttpConnector getClientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, properties.getTimeout())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(
                            new ReadTimeoutHandler(properties.getTimeout(), MILLISECONDS));
                    connection.addHandlerLast(
                            new WriteTimeoutHandler(properties.getTimeout(), MILLISECONDS));
                }));
    }
}
