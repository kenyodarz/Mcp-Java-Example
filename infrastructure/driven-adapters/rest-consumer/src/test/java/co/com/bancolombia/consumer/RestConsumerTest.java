package co.com.bancolombia.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class RestConsumerTest {

    // ==================== TEST DOUBLES ====================
    // Mock del servidor HTTP que simula las respuestas de la API externa
    private MockWebServer mockWebServerDouble;

    // Sistema bajo prueba (SUT): El cliente HTTP consumer que realiza las llamadas
    private RestConsumer restConsumerSUT;

    @BeforeEach
    void setUp() throws Exception {
        // Inicializar el mock server
        mockWebServerDouble = new MockWebServer();
        mockWebServerDouble.start();
        // Inicializar el cliente consumer apuntando al mock server
        restConsumerSUT = new RestConsumer(
                WebClient.builder().baseUrl(mockWebServerDouble.url("/").toString()).build());
    }

    @AfterEach
    void tearDown() throws Exception {
        // Limpiar recursos del mock server
        mockWebServerDouble.shutdown();
    }

    @Test
    void shouldGetCharacterById() throws Exception {
        // ==================== GIVEN ====================
        // Preparar una respuesta JSON de un character desde la API
        MockResponse characterJsonResponse = new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": 1,
                          "name": "Homer Simpson",
                          "gender": "Male",
                          "portrait_path": "/character/1.webp"
                        }
                        """);
        mockWebServerDouble.enqueue(characterJsonResponse);

        // ==================== WHEN ====================
        // Ejecutar la llamada al cliente consumer para obtener el character

        // ==================== THEN ====================
        // Verificar que los datos se deserializan correctamente
        StepVerifier.create(restConsumerSUT.getCharacterById(1))
                .assertNext(characterResponse -> {
                    assertEquals(1, characterResponse.getId());
                    assertEquals("Homer Simpson", characterResponse.getName());
                    assertEquals("Male", characterResponse.getGender());
                })
                .verifyComplete();

        // Verificar que la solicitud HTTP se realizó al endpoint correcto
        assertEquals("/characters/1", mockWebServerDouble.takeRequest().getPath());
    }

    @Test
    void shouldGetEpisodeById() throws Exception {
        // ==================== GIVEN ====================
        // Preparar una respuesta JSON de un episode desde la API
        MockResponse episodeJsonResponse = new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": 7,
                          "name": "The Call of the Simpsons",
                          "episode_number": 7,
                          "season": 1,
                          "image_path": "/episode/7.webp"
                        }
                        """);
        mockWebServerDouble.enqueue(episodeJsonResponse);

        // ==================== WHEN ====================
        // Ejecutar la llamada al cliente consumer para obtener el episode

        // ==================== THEN ====================
        // Verificar que los datos se deserializan correctamente
        StepVerifier.create(restConsumerSUT.getEpisodeById(7))
                .assertNext(episodeResponse -> {
                    assertEquals(7, episodeResponse.getId());
                    assertEquals("The Call of the Simpsons", episodeResponse.getName());
                    assertEquals(7, episodeResponse.getEpisodeNumber());
                    assertEquals(1, episodeResponse.getSeason());
                })
                .verifyComplete();

        // Verificar que la solicitud HTTP se realizó al endpoint correcto
        assertEquals("/episodes/7", mockWebServerDouble.takeRequest().getPath());
    }

    @Test
    void shouldGetLocationById() throws Exception {
        // ==================== GIVEN ====================
        // Preparar una respuesta JSON de una location desde la API
        MockResponse locationJsonResponse = new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": 9,
                          "name": "Moe's Tavern",
                          "description": "Bar clásico de Springfield"
                        }
                        """);
        mockWebServerDouble.enqueue(locationJsonResponse);

        // ==================== WHEN ====================
        // Ejecutar la llamada al cliente consumer para obtener la location

        // ==================== THEN ====================
        // Verificar que los datos se deserializan correctamente
        StepVerifier.create(restConsumerSUT.getLocationById(9))
                .assertNext(locationResponse -> {
                    assertEquals(9, locationResponse.getId());
                    assertEquals("Moe's Tavern", locationResponse.getName());
                    assertEquals("Bar clásico de Springfield", locationResponse.getDescription());
                })
                .verifyComplete();

        // Verificar que la solicitud HTTP se realizó al endpoint correcto
        assertEquals("/locations/9", mockWebServerDouble.takeRequest().getPath());
    }
}

