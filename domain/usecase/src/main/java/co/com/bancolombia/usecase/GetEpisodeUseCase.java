package co.com.bancolombia.usecase;

import co.com.bancolombia.model.simpsons.SimpsonsEpisode;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
public record GetEpisodeUseCase(SimpsonsGateway simpsonsGateway) {

    public Mono<SimpsonsEpisode> execute(Integer id) {
        log.info(String.format("Getting Simpsons episode by id: %s", id));
        return simpsonsGateway.getEpisodeById(id);
    }
}

