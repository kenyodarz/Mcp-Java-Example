package co.com.bancolombia.consumer.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.bancolombia.consumer.RestConsumer;
import co.com.bancolombia.consumer.SimpsonsCharacterResponse;
import co.com.bancolombia.consumer.SimpsonsEpisodeResponse;
import co.com.bancolombia.consumer.SimpsonsLocationResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SimpsonsApiAdapterTest {

    @Mock
    private RestConsumer client;

    private SimpsonsApiAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SimpsonsApiAdapter(client);
    }

    @Test
    void shouldMapCharacterResponseToUserInfoIncludingNestedObjects() {
        SimpsonsCharacterResponse response = SimpsonsCharacterResponse.builder()
                .id(1)
                .age(39)
                .birthdate("1956-05-12")
                .description("Padre de familia")
                .gender("Male")
                .name("Homer Simpson")
                .occupation("Safety Inspector")
                .portraitPath("/character/1.webp")
                .status("Alive")
                .phrases(List.of("Doh!"))
                .firstAppearanceEp(SimpsonsCharacterResponse.EpisodeResponse.builder()
                        .id(10)
                        .name("Simpsons Roasting on an Open Fire")
                        .episodeNumber(1)
                        .season(1)
                        .build())
                .firstAppearanceSh(SimpsonsCharacterResponse.ShortResponse.builder()
                        .id(20)
                        .name("Good Night")
                        .episodeNumber(1)
                        .season(1)
                        .build())
                .build();

        when(client.getCharacterById(1)).thenReturn(Mono.just(response));

        StepVerifier.create(adapter.getUserInfoById(1))
                .assertNext(userInfo -> {
                    assertEquals(1, userInfo.getId());
                    assertEquals("Homer Simpson", userInfo.getName());
                    assertEquals("Safety Inspector", userInfo.getOccupation());
                    assertEquals("Alive", userInfo.getStatus());
                    assertEquals("Simpsons Roasting on an Open Fire",
                            userInfo.getFirstAppearanceEp().getName());
                    assertEquals("Good Night", userInfo.getFirstAppearanceSh().getName());
                })
                .verifyComplete();

        verify(client).getCharacterById(1);
    }

    @Test
    void shouldMapCharacterResponseToUserInfoWhenNestedObjectsAreNull() {
        SimpsonsCharacterResponse response = SimpsonsCharacterResponse.builder()
                .id(2)
                .name("Bart Simpson")
                .build();

        when(client.getCharacterById(2)).thenReturn(Mono.just(response));

        StepVerifier.create(adapter.getUserInfoById(2))
                .assertNext(userInfo -> {
                    assertEquals(2, userInfo.getId());
                    assertEquals("Bart Simpson", userInfo.getName());
                    assertNull(userInfo.getFirstAppearanceEp());
                    assertNull(userInfo.getFirstAppearanceSh());
                })
                .verifyComplete();
    }

    @Test
    void shouldMapCharacterResponseToCharacter() {
        SimpsonsCharacterResponse response = SimpsonsCharacterResponse.builder()
                .id(3)
                .name("Lisa Simpson")
                .phrases(List.of("If anyone wants me, I'll be in my room"))
                .build();

        when(client.getCharacterById(3)).thenReturn(Mono.just(response));

        StepVerifier.create(adapter.getCharacterById(3))
                .assertNext(character -> {
                    assertEquals(3, character.getId());
                    assertEquals("Lisa Simpson", character.getName());
                    assertEquals(1, character.getPhrases().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldMapEpisodeResponseToEpisode() {
        SimpsonsEpisodeResponse response = SimpsonsEpisodeResponse.builder()
                .id(4)
                .name("Cape Feare")
                .episodeNumber(2)
                .season(5)
                .build();

        when(client.getEpisodeById(4)).thenReturn(Mono.just(response));

        StepVerifier.create(adapter.getEpisodeById(4))
                .assertNext(episode -> {
                    assertEquals(4, episode.getId());
                    assertEquals("Cape Feare", episode.getName());
                    assertEquals(2, episode.getEpisodeNumber());
                    assertEquals(5, episode.getSeason());
                })
                .verifyComplete();
    }

    @Test
    void shouldMapLocationResponseToLocation() {
        SimpsonsLocationResponse response = SimpsonsLocationResponse.builder()
                .id(5)
                .name("Springfield Elementary")
                .description("Escuela primaria")
                .build();

        when(client.getLocationById(5)).thenReturn(Mono.just(response));

        StepVerifier.create(adapter.getLocationById(5))
                .assertNext(location -> {
                    assertEquals(5, location.getId());
                    assertEquals("Springfield Elementary", location.getName());
                    assertEquals("Escuela primaria", location.getDescription());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateRestConsumerErrors() {
        when(client.getEpisodeById(77)).thenReturn(
                Mono.error(new IllegalStateException("remote failure")));

        StepVerifier.create(adapter.getEpisodeById(77))
                .expectErrorMessage("remote failure")
                .verify();
    }
}

