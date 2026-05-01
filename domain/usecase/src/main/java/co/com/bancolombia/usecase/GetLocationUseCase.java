package co.com.bancolombia.usecase;

import co.com.bancolombia.model.simpsons.SimpsonsLocation;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
public record GetLocationUseCase(SimpsonsGateway simpsonsGateway) {

    public Mono<SimpsonsLocation> execute(Integer id) {
        log.info(String.format("Getting Simpsons location by id: %s", id));
        return simpsonsGateway.getLocationById(id);
    }
}

