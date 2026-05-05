package co.com.bancolombia.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.bancolombia.model.simpsons.SimpsonsCharacter;
import co.com.bancolombia.model.simpsons.SimpsonsEpisode;
import co.com.bancolombia.model.simpsons.SimpsonsLocation;
import co.com.bancolombia.model.simpsons.gateways.SimpsonsGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SimpsonsUseCasesTest {

    @Mock
    private SimpsonsGateway simpsonsGateway;

    private GetCharacterUseCase getCharacterUseCase;
    private GetEpisodeUseCase getEpisodeUseCase;
    private GetLocationUseCase getLocationUseCase;

    @BeforeEach
    void setUp() {
        getCharacterUseCase = new GetCharacterUseCase(simpsonsGateway);
        getEpisodeUseCase = new GetEpisodeUseCase(simpsonsGateway);
        getLocationUseCase = new GetLocationUseCase(simpsonsGateway);
    }

    @Test
    void shouldGetCharacterById() {
        SimpsonsCharacter character = SimpsonsCharacter.builder().id(1).name("Homer Simpson")
                .build();
        when(simpsonsGateway.getCharacterById(1)).thenReturn(Mono.just(character));

        StepVerifier.create(getCharacterUseCase.execute(1))
                .expectNext(character)
                .verifyComplete();

        verify(simpsonsGateway).getCharacterById(1);
    }

    @Test
    void shouldGetEpisodeById() {
        SimpsonsEpisode episode = SimpsonsEpisode.builder().id(7).name("The Call of the Simpsons")
                .build();
        when(simpsonsGateway.getEpisodeById(7)).thenReturn(Mono.just(episode));

        StepVerifier.create(getEpisodeUseCase.execute(7))
                .expectNext(episode)
                .verifyComplete();

        verify(simpsonsGateway).getEpisodeById(7);
    }

    @Test
    void shouldGetLocationById() {
        SimpsonsLocation location = SimpsonsLocation.builder().id(3).name("Moe's Tavern").build();
        when(simpsonsGateway.getLocationById(3)).thenReturn(Mono.just(location));

        StepVerifier.create(getLocationUseCase.execute(3))
                .expectNext(location)
                .verifyComplete();

        verify(simpsonsGateway).getLocationById(3);
    }
}

