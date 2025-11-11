package co.com.bancolombia.consumer.config.properties;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "adapter.restconsumer")
public class RestConsumerProperties {

    private String url;
    private int timeout;
    private Map<String, String> headers;
}
