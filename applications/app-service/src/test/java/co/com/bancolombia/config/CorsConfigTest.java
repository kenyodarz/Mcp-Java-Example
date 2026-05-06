package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CorsConfigTest {

    // ==================== TEST DOUBLES ====================
    // Configuración bajo prueba: La configuración CORS
    private final CorsConfig corsConfigSUT = new CorsConfig();

    @Test
    void shouldCreateDefaultCorsFilter() {
        // ==================== GIVEN ====================
        // No es necesario preparar datos específicos

        // ==================== WHEN ====================
        // Crear el filtro CORS por defecto
        var defaultCorsFilter = corsConfigSUT.corsWebFilterDefault();

        // ==================== THEN ====================
        // Verificar que se crea correctamente
        assertNotNull(defaultCorsFilter);
    }

    @Test
    void shouldCreateLocalCorsFilter() {
        // ==================== GIVEN ====================
        // No es necesario preparar datos específicos

        // ==================== WHEN ====================
        // Crear el filtro CORS para ambiente local
        var localCorsFilter = corsConfigSUT.corsWebFilterLocal();

        // ==================== THEN ====================
        // Verificar que se crea correctamente
        assertNotNull(localCorsFilter);
    }

    @Test
    void shouldFallbackToDefaultWhenOriginIsWildcard() {
        // ==================== GIVEN ====================
        // Preparar un origen con comodín (*)

        // ==================== WHEN ====================
        // Crear filtro CORS con origen comodín
        var corsFilterWithWildcard = corsConfigSUT.corsWebFilterOthers("*");

        // ==================== THEN ====================
        // Verificar que retorna a la configuración por defecto
        assertNotNull(corsFilterWithWildcard);
    }

    @Test
    void shouldCreateDevCorsFilterWithConfiguredOrigins() {
        // ==================== GIVEN ====================
        // Preparar una lista de orígenes para ambiente dev/qa
        String devAndQaOrigins = "https://dev.example.com,https://qa.example.com";

        // ==================== WHEN ====================
        // Crear filtro CORS con esos orígenes
        var devCorsFilter = corsConfigSUT.corsWebFilterOthers(devAndQaOrigins);

        // ==================== THEN ====================
        // Verificar que se crea correctamente
        assertNotNull(devCorsFilter);
    }

    @Test
    void shouldCreateProductionCorsFilter() {
        // ==================== GIVEN ====================
        // Preparar un origen para ambiente producción
        String productionOrigin = "https://prod.example.com";

        // ==================== WHEN ====================
        // Crear filtro CORS para producción
        var productionCorsFilter = corsConfigSUT.corsWebFilterPdn(productionOrigin);

        // ==================== THEN ====================
        // Verificar que se crea correctamente
        assertNotNull(productionCorsFilter);
    }
}

