package co.com.bancolombia.consumer;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Log
public class RestConsumer /* implements Gateway from domain */ {

    private static final String FALLBACK_ERROR_SUFFIX = ". Using fallback data. Error: ";
    private static final String API_INFO_ERROR_MESSAGE = "No se pudo obtener información de la API. ";

    private final WebClient client;

    @CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getCharacterByIdFallback")
    @Retry(name = "simpsonsApi")
    @TimeLimiter(name = "simpsonsApi")
    public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
        log.info("Fetching character with id: " + id);
        return client.get()
                .uri("/characters/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsCharacterResponse.class)
                .doOnError(error -> log.warning(
                        "Error fetching character " + id + ": " + error.getMessage()));
    }

    @CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getEpisodeByIdFallback")
    @Retry(name = "simpsonsApi")
    @TimeLimiter(name = "simpsonsApi")
    public Mono<SimpsonsEpisodeResponse> getEpisodeById(int id) {
        log.info("Fetching episode with id: " + id);
        return client.get()
                .uri("/episodes/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsEpisodeResponse.class)
                .doOnError(error -> log.warning(
                        "Error fetching episode " + id + ": " + error.getMessage()));
    }

    @CircuitBreaker(name = "simpsonsApi", fallbackMethod = "getLocationByIdFallback")
    @Retry(name = "simpsonsApi")
    @TimeLimiter(name = "simpsonsApi")
    public Mono<SimpsonsLocationResponse> getLocationById(int id) {
        log.info("Fetching location with id: " + id);
        return client.get()
                .uri("/locations/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsLocationResponse.class)
                .doOnError(error -> log.warning(
                        "Error fetching location " + id + ": " + error.getMessage()));
    }

    // Fallback methods
    @SuppressWarnings("unused")
    public Mono<SimpsonsCharacterResponse> getCharacterByIdFallback(int id, Exception ex) {
        log.warning("Circuit breaker or retry exhausted for character " + id + FALLBACK_ERROR_SUFFIX
                + ex.getMessage());
        return Mono.just(SimpsonsCharacterResponse.builder()
                .id(-1)
                .name("Personaje desconocido")
                .status("FALLBACK_DATA")
                .description(API_INFO_ERROR_MESSAGE + ex.getMessage())
                .age(0)
                .build());
    }

    @SuppressWarnings("unused")
    public Mono<SimpsonsEpisodeResponse> getEpisodeByIdFallback(int id, Exception ex) {
        log.warning("Circuit breaker or retry exhausted for episode " + id + FALLBACK_ERROR_SUFFIX
                + ex.getMessage());
        return Mono.just(SimpsonsEpisodeResponse.builder()
                .id(-1)
                .name("Episodio desconocido")
                .season(0)
                .episodeNumber(0)
                .description(API_INFO_ERROR_MESSAGE + ex.getMessage())
                .build());
    }

    @SuppressWarnings("unused")
    public Mono<SimpsonsLocationResponse> getLocationByIdFallback(int id, Exception ex) {
        log.warning("Circuit breaker or retry exhausted for location " + id + FALLBACK_ERROR_SUFFIX
                + ex.getMessage());
        return Mono.just(SimpsonsLocationResponse.builder()
                .id(-1)
                .name("Ubicación desconocida")
                .description(API_INFO_ERROR_MESSAGE + ex.getMessage())
                .build());
    }
}
