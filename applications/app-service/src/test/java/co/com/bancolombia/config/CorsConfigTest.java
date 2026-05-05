package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CorsConfigTest {

    private final CorsConfig config = new CorsConfig();

    @Test
    void shouldCreateDefaultCorsFilter() {
        assertNotNull(config.corsWebFilterDefault());
    }

    @Test
    void shouldCreateLocalCorsFilter() {
        assertNotNull(config.corsWebFilterLocal());
    }

    @Test
    void shouldFallbackToDefaultWhenOriginIsWildcard() {
        assertNotNull(config.corsWebFilterOthers("*"));
    }

    @Test
    void shouldCreateDevCorsFilterWithConfiguredOrigins() {
        assertNotNull(config.corsWebFilterOthers("https://dev.example.com,https://qa.example.com"));
    }

    @Test
    void shouldCreateProductionCorsFilter() {
        assertNotNull(config.corsWebFilterPdn("https://prod.example.com"));
    }
}

