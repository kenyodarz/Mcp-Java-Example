package co.com.bancolombia.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RestConsumer /* implements Gateway from domain */ {

    private final WebClient client;

    public Mono<SimpsonsCharacterResponse> getCharacterById(int id) {
        return client.get()
                .uri("/characters/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsCharacterResponse.class);
    }

    public Mono<SimpsonsEpisodeResponse> getEpisodeById(int id) {
        return client.get()
                .uri("/episodes/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsEpisodeResponse.class);
    }

    public Mono<SimpsonsLocationResponse> getLocationById(int id) {
        return client.get()
                .uri("/locations/{id}", id)
                .retrieve()
                .bodyToMono(SimpsonsLocationResponse.class);
    }
}
