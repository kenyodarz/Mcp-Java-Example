package co.com.bancolombia.security.apikey;

import co.com.bancolombia.model.apikey.gateways.ApiKeyGateway;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Component
public class ApiKeyCacheLoader {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyCacheLoader.class);

    private final ApiKeyGateway gateway;
    private final InMemoryApiKeyRepository repository;

    public ApiKeyCacheLoader(ApiKeyGateway gateway, InMemoryApiKeyRepository repository) {
        this.gateway = gateway;
        this.repository = repository;
    }

    @PostConstruct
    public void loadCache() {
        log.info("📥 Cargando API Keys activas en cache...");

        repository.clear();

        gateway.findAllActive()
                .map(apiKey -> new InMemoryApiKeyEntity(
                        apiKey.getId(),
                        apiKey.getSecretHash(),
                        Boolean.TRUE.equals(apiKey.getEnabled())
                ))
                .doOnNext(repository::put)
                .doOnComplete(() -> log.info("✅ API Keys cargadas en memoria"))
                .doOnError(e -> log.error("❌ Error cargando cache de API Keys", e))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
