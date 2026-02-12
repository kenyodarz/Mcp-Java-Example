package co.com.bancolombia.model.simpsons.gateways;

import co.com.bancolombia.model.simpsons.SimpsonsCharacter;
import co.com.bancolombia.model.simpsons.SimpsonsEpisode;
import co.com.bancolombia.model.simpsons.SimpsonsLocation;
import reactor.core.publisher.Mono;

public interface SimpsonsGateway {

    Mono<SimpsonsCharacter> getCharacterById(Integer id);

    Mono<SimpsonsEpisode> getEpisodeById(Integer id);

    Mono<SimpsonsLocation> getLocationById(Integer id);
}
