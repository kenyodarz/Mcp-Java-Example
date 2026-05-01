package co.com.bancolombia.usecase;

import co.com.bancolombia.model.simpsons.SimpsonsCharacter;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
public record GetCharacterUseCase(SimpsonsGateway simpsonsGateway) {

    public Mono<SimpsonsCharacter> execute(Integer id) {
        log.info(String.format("Getting Simpsons character by id: %s", id));
        return simpsonsGateway.getCharacterById(id);
    }
}

