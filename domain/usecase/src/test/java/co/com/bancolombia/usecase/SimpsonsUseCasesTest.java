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

    // ==================== TEST DOUBLES ====================
    // Mock del gateway que proporciona acceso a los datos de Simpsons
    @Mock
    private SimpsonsGateway simpsonsGatewayMock;

    // Sistemas bajo prueba (SUTs): Casos de uso para obtener datos de Simpsons
    private GetCharacterUseCase getCharacterUseCaseSUT;
    private GetEpisodeUseCase getEpisodeUseCaseSUT;
    private GetLocationUseCase getLocationUseCaseSUT;

    @BeforeEach
    void setUp() {
        // Inicializar los casos de uso con el mock del gateway
        getCharacterUseCaseSUT = new GetCharacterUseCase(simpsonsGatewayMock);
        getEpisodeUseCaseSUT = new GetEpisodeUseCase(simpsonsGatewayMock);
        getLocationUseCaseSUT = new GetLocationUseCase(simpsonsGatewayMock);
    }

    @Test
    void shouldGetCharacterById() {
        // ==================== GIVEN ====================
        // Preparar datos de un character esperado
        SimpsonsCharacter expectedCharacter = SimpsonsCharacter.builder()
                .id(1)
                .name("Homer Simpson")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar el character
        when(simpsonsGatewayMock.getCharacterById(1)).thenReturn(Mono.just(expectedCharacter));

        // ==================== THEN ====================
        // Verificar que el caso de uso retorna el character esperado
        StepVerifier.create(getCharacterUseCaseSUT.execute(1))
                .expectNext(expectedCharacter)
                .verifyComplete();

        // Verificar que el gateway fue invocado correctamente
        verify(simpsonsGatewayMock).getCharacterById(1);
    }

    @Test
    void shouldGetEpisodeById() {
        // ==================== GIVEN ====================
        // Preparar datos de un episode esperado
        SimpsonsEpisode expectedEpisode = SimpsonsEpisode.builder()
                .id(7)
                .name("The Call of the Simpsons")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar el episode
        when(simpsonsGatewayMock.getEpisodeById(7)).thenReturn(Mono.just(expectedEpisode));

        // ==================== THEN ====================
        // Verificar que el caso de uso retorna el episode esperado
        StepVerifier.create(getEpisodeUseCaseSUT.execute(7))
                .expectNext(expectedEpisode)
                .verifyComplete();

        // Verificar que el gateway fue invocado correctamente
        verify(simpsonsGatewayMock).getEpisodeById(7);
    }

    @Test
    void shouldGetLocationById() {
        // ==================== GIVEN ====================
        // Preparar datos de una location esperada
        SimpsonsLocation expectedLocation = SimpsonsLocation.builder()
                .id(3)
                .name("Moe's Tavern")
                .build();

        // ==================== WHEN ====================
        // Configurar el mock para retornar la location
        when(simpsonsGatewayMock.getLocationById(3)).thenReturn(Mono.just(expectedLocation));

        // ==================== THEN ====================
        // Verificar que el caso de uso retorna la location esperada
        StepVerifier.create(getLocationUseCaseSUT.execute(3))
                .expectNext(expectedLocation)
                .verifyComplete();

        // Verificar que el gateway fue invocado correctamente
        verify(simpsonsGatewayMock).getLocationById(3);
    }
}

