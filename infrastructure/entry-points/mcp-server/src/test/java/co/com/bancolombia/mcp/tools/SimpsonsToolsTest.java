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

    // ==================== TEST DOUBLES ====================
    // Mocks de los casos de uso para obtener datos de Simpsons
    @Mock
    private GetCharacterUseCase getCharacterUseCaseMock;
    @Mock
    private GetEpisodeUseCase getEpisodeUseCaseMock;
    @Mock
    private GetLocationUseCase getLocationUseCaseMock;

    // Sistema bajo prueba (SUT): Las herramientas MCP que exponen los casos de uso
    private SimpsonsTools simpsonsToolsSUT;

    @BeforeEach
    void setUp() {
        // Inicializar las herramientas MCP con los mocks de los casos de uso
        simpsonsToolsSUT = new SimpsonsTools(getCharacterUseCaseMock, getEpisodeUseCaseMock,
                getLocationUseCaseMock);
    }

    @Test
    void shouldGetCharacterThroughUseCase() {
        // ==================== GIVEN ====================
        // Preparar un character esperado con datos de Homer
        SimpsonsCharacter expectedCharacter = SimpsonsCharacter.builder()
                .id(1)
                .name("Homer Simpson")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar el character
        when(getCharacterUseCaseMock.execute(1)).thenReturn(Mono.just(expectedCharacter));

        // ==================== THEN ====================
        // Verificar que la herramienta MCP retorna el character correcto
        StepVerifier.create(simpsonsToolsSUT.getCharacter(1))
                .assertNext(
                        characterResult -> assertEquals("Homer Simpson", characterResult.getName()))
                .verifyComplete();

        // Verificar que el caso de uso fue invocado correctamente
        verify(getCharacterUseCaseMock).execute(1);
    }

    @Test
    void shouldGetEpisodeThroughUseCase() {
        // ==================== GIVEN ====================
        // Preparar un episode esperado
        SimpsonsEpisode expectedEpisode = SimpsonsEpisode.builder()
                .id(2)
                .name("Bart the Genius")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar el episode
        when(getEpisodeUseCaseMock.execute(2)).thenReturn(Mono.just(expectedEpisode));

        // ==================== THEN ====================
        // Verificar que la herramienta MCP retorna el episode correcto
        StepVerifier.create(simpsonsToolsSUT.getEpisode(2))
                .assertNext(
                        episodeResult -> assertEquals("Bart the Genius", episodeResult.getName()))
                .verifyComplete();

        // Verificar que el caso de uso fue invocado correctamente
        verify(getEpisodeUseCaseMock).execute(2);
    }

    @Test
    void shouldGetLocationThroughUseCase() {
        // ==================== GIVEN ====================
        // Preparar una location esperada
        SimpsonsLocation expectedLocation = SimpsonsLocation.builder()
                .id(3)
                .name("Springfield Nuclear Power Plant")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar la location
        when(getLocationUseCaseMock.execute(3)).thenReturn(Mono.just(expectedLocation));

        // ==================== THEN ====================
        // Verificar que la herramienta MCP retorna la location correcta
        StepVerifier.create(simpsonsToolsSUT.getLocation(3))
                .assertNext(
                        locationResult -> assertEquals("Springfield Nuclear Power Plant",
                                locationResult.getName()))
                .verifyComplete();

        // Verificar que el caso de uso fue invocado correctamente
        verify(getLocationUseCaseMock).execute(3);
    }
}

