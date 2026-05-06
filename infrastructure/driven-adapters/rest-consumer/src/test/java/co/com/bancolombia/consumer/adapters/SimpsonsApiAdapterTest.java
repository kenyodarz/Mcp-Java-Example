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

    // ==================== TEST DOUBLES ====================
    // Mock del cliente REST consumer que hace las llamadas externas a la API
    @Mock
    private RestConsumer restConsumerClientMock;

    // Sistema bajo prueba (SUT): El adaptador que implementa la lógica de mapeo
    private SimpsonsApiAdapter simpsonsApiAdapterSUT;

    @BeforeEach
    void setUp() {
        // Inicializar el adaptador con el mock del cliente
        simpsonsApiAdapterSUT = new SimpsonsApiAdapter(restConsumerClientMock);
    }

    @Test
    void shouldMapCharacterResponseToUserInfoIncludingNestedObjects() {
        // ==================== GIVEN ====================
        // Preparar una respuesta de character con objetos anidados (episodes y shorts)
        SimpsonsCharacterResponse characterResponseWithNestedObjects = SimpsonsCharacterResponse.builder()
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

        // ==================== WHEN ====================
        // Configurar el mock para retornar la respuesta cuando se solicite el character por ID
        when(restConsumerClientMock.getCharacterById(1)).thenReturn(
                Mono.just(characterResponseWithNestedObjects));

        // ==================== THEN ====================
        // Verificar que los datos se mapean correctamente incluyendo objetos anidados
        StepVerifier.create(simpsonsApiAdapterSUT.getUserInfoById(1))
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

        // Verificar que el cliente fue llamado correctamente
        verify(restConsumerClientMock).getCharacterById(1);
    }

    @Test
    void shouldMapCharacterResponseToUserInfoWhenNestedObjectsAreNull() {
        // ==================== GIVEN ====================
        // Preparar una respuesta de character con objetos anidados nulos
        SimpsonsCharacterResponse characterResponseWithNullNestedObjects = SimpsonsCharacterResponse.builder()
                .id(2)
                .name("Bart Simpson")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar la respuesta cuando se solicite el character por ID
        when(restConsumerClientMock.getCharacterById(2)).thenReturn(
                Mono.just(characterResponseWithNullNestedObjects));

        // ==================== THEN ====================
        // Verificar que la conversión maneja correctamente los valores nulos
        StepVerifier.create(simpsonsApiAdapterSUT.getUserInfoById(2))
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
        // ==================== GIVEN ====================
        // Preparar una respuesta de character con frases asociadas
        SimpsonsCharacterResponse characterResponseWithPhrases = SimpsonsCharacterResponse.builder()
                .id(3)
                .name("Lisa Simpson")
                .phrases(List.of("If anyone wants me, I'll be in my room"))
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar la respuesta cuando se solicite el character por ID
        when(restConsumerClientMock.getCharacterById(3)).thenReturn(
                Mono.just(characterResponseWithPhrases));

        // ==================== THEN ====================
        // Verificar que el mapping de character es correcto incluyendo frases
        StepVerifier.create(simpsonsApiAdapterSUT.getCharacterById(3))
                .assertNext(character -> {
                    assertEquals(3, character.getId());
                    assertEquals("Lisa Simpson", character.getName());
                    assertEquals(1, character.getPhrases().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldMapEpisodeResponseToEpisode() {
        // ==================== GIVEN ====================
        // Preparar una respuesta de episode con todos sus datos
        SimpsonsEpisodeResponse episodeResponse = SimpsonsEpisodeResponse.builder()
                .id(4)
                .name("Cape Feare")
                .episodeNumber(2)
                .season(5)
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar la respuesta cuando se solicite el episode por ID
        when(restConsumerClientMock.getEpisodeById(4)).thenReturn(Mono.just(episodeResponse));

        // ==================== THEN ====================
        // Verificar que el mapping de episode es correcto
        StepVerifier.create(simpsonsApiAdapterSUT.getEpisodeById(4))
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
        // ==================== GIVEN ====================
        // Preparar una respuesta de location con descripción
        SimpsonsLocationResponse locationResponse = SimpsonsLocationResponse.builder()
                .id(5)
                .name("Springfield Elementary")
                .description("Escuela primaria")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar la respuesta cuando se solicite la location por ID
        when(restConsumerClientMock.getLocationById(5)).thenReturn(Mono.just(locationResponse));

        // ==================== THEN ====================
        // Verificar que el mapping de location es correcto
        StepVerifier.create(simpsonsApiAdapterSUT.getLocationById(5))
                .assertNext(location -> {
                    assertEquals(5, location.getId());
                    assertEquals("Springfield Elementary", location.getName());
                    assertEquals("Escuela primaria", location.getDescription());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateRestConsumerErrors() {
        // ==================== GIVEN ====================
        // Preparar un escenario donde el cliente retorna un error
        Exception remoteFailureException = new IllegalStateException("remote failure");

        // ==================== WHEN ====================
        // Configurar el mock para retornar un error cuando se solicite el episode por ID
        when(restConsumerClientMock.getEpisodeById(77)).thenReturn(
                Mono.error(remoteFailureException));

        // ==================== THEN ====================
        // Verificar que el error se propaga correctamente sin ser capturado
        StepVerifier.create(simpsonsApiAdapterSUT.getEpisodeById(77))
                .expectErrorMessage("remote failure")
                .verify();
    }
}

