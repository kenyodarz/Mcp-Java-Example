// applications/app-service/src/main/java/co/com/bancolombia/health/ApiKeyCacheHealthIndicator.java

package co.com.bancolombia.health;

import co.com.bancolombia.security.apikey.InMemoryCachedApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component("apiKeyCache")
@RequiredArgsConstructor
public class ApiKeyCacheHealthIndicator implements ReactiveHealthIndicator {

    private final InMemoryCachedApiKeyRepository cache;

    @Override
    public Mono<Health> health() {
        return Mono.fromCallable(() -> {
                    var metrics = cache.getCacheMetrics();

                    if (metrics.size() == 0) {
                        return Health.down()
                                .withDetail("reason", "API Key cache is empty")
                                .build();
                    }

                    return Health.up()
                            .withDetail("cachedKeys", metrics.size())
                            .withDetail("hitRatio", String.format("%.2f%%", metrics.hitRate() * 100))
                            .withDetail("hitCount", metrics.hitCount())
                            .withDetail("missCount", metrics.missCount())
                            .withDetail("evictionCount", metrics.evictionCount())
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}