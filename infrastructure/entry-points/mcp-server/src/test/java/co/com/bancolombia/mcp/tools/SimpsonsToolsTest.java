package co.com.bancolombia.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.bancolombia.model.simpsons.SimpsonsCharacter;
import co.com.bancolombia.model.simpsons.SimpsonsEpisode;
import co.com.bancolombia.model.simpsons.SimpsonsLocation;
import co.com.bancolombia.usecase.GetCharacterUseCase;
import co.com.bancolombia.usecase.GetEpisodeUseCase;
import co.com.bancolombia.usecase.GetLocationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SimpsonsToolsTest {

    @Mock
    private GetCharacterUseCase getCharacterUseCase;
    @Mock
    private GetEpisodeUseCase getEpisodeUseCase;
    @Mock
    private GetLocationUseCase getLocationUseCase;

    private SimpsonsTools tools;

    @BeforeEach
    void setUp() {
        tools = new SimpsonsTools(getCharacterUseCase, getEpisodeUseCase, getLocationUseCase);
    }

    @Test
    void shouldGetCharacterThroughUseCase() {
        SimpsonsCharacter character = SimpsonsCharacter.builder().id(1).name("Homer Simpson")
                .build();
        when(getCharacterUseCase.execute(1)).thenReturn(Mono.just(character));

        StepVerifier.create(tools.getCharacter(1))
                .assertNext(result -> assertEquals("Homer Simpson", result.getName()))
                .verifyComplete();

        verify(getCharacterUseCase).execute(1);
    }

    @Test
    void shouldGetEpisodeThroughUseCase() {
        SimpsonsEpisode episode = SimpsonsEpisode.builder().id(2).name("Bart the Genius").build();
        when(getEpisodeUseCase.execute(2)).thenReturn(Mono.just(episode));

        StepVerifier.create(tools.getEpisode(2))
                .assertNext(result -> assertEquals("Bart the Genius", result.getName()))
                .verifyComplete();

        verify(getEpisodeUseCase).execute(2);
    }

    @Test
    void shouldGetLocationThroughUseCase() {
        SimpsonsLocation location = SimpsonsLocation.builder().id(3)
                .name("Springfield Nuclear Power Plant").build();
        when(getLocationUseCase.execute(3)).thenReturn(Mono.just(location));

        StepVerifier.create(tools.getLocation(3))
                .assertNext(
                        result -> assertEquals("Springfield Nuclear Power Plant", result.getName()))
                .verifyComplete();

        verify(getLocationUseCase).execute(3);
    }
}

