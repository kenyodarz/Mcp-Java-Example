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

    private MockWebServer server;
    private RestConsumer consumer;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        consumer = new RestConsumer(
                WebClient.builder().baseUrl(server.url("/").toString()).build());
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void shouldGetCharacterById() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": 1,
                          "name": "Homer Simpson",
                          "gender": "Male",
                          "portrait_path": "/character/1.webp"
                        }
                        """));

        StepVerifier.create(consumer.getCharacterById(1))
                .assertNext(response -> {
                    assertEquals(1, response.getId());
                    assertEquals("Homer Simpson", response.getName());
                    assertEquals("Male", response.getGender());
                })
                .verifyComplete();

        assertEquals("/characters/1", server.takeRequest().getPath());
    }

    @Test
    void shouldGetEpisodeById() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": 7,
                          "name": "The Call of the Simpsons",
                          "episode_number": 7,
                          "season": 1,
                          "image_path": "/episode/7.webp"
                        }
                        """));

        StepVerifier.create(consumer.getEpisodeById(7))
                .assertNext(response -> {
                    assertEquals(7, response.getId());
                    assertEquals("The Call of the Simpsons", response.getName());
                    assertEquals(7, response.getEpisodeNumber());
                    assertEquals(1, response.getSeason());
                })
                .verifyComplete();

        assertEquals("/episodes/7", server.takeRequest().getPath());
    }

    @Test
    void shouldGetLocationById() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": 9,
                          "name": "Moe's Tavern",
                          "description": "Bar clásico de Springfield"
                        }
                        """));

        StepVerifier.create(consumer.getLocationById(9))
                .assertNext(response -> {
                    assertEquals(9, response.getId());
                    assertEquals("Moe's Tavern", response.getName());
                    assertEquals("Bar clásico de Springfield", response.getDescription());
                })
                .verifyComplete();

        assertEquals("/locations/9", server.takeRequest().getPath());
    }
}

