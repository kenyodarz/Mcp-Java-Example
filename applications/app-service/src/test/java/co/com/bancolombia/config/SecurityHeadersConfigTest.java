package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SecurityHeadersConfigTest {

    // ==================== TEST DOUBLES ====================
    // Configuración bajo prueba: Los headers de seguridad
    private SecurityHeadersConfig securityHeadersConfigSUT;

    @BeforeEach
    void setUp() {
        // Inicializar la configuración
        securityHeadersConfigSUT = new SecurityHeadersConfig();
        // Inyectar los valores de configuración usando reflexión
        ReflectionTestUtils.setField(securityHeadersConfigSUT, "contentSecurityPolicy",
                "default-src 'self'");
        ReflectionTestUtils.setField(securityHeadersConfigSUT, "strictTransportSecurity",
                "max-age=1000");
        ReflectionTestUtils.setField(securityHeadersConfigSUT, "xContentTypeOptions", "nosniff");
        ReflectionTestUtils.setField(securityHeadersConfigSUT, "cacheControl", "no-store");
        ReflectionTestUtils.setField(securityHeadersConfigSUT, "pragmaValue", "no-cache");
        ReflectionTestUtils.setField(securityHeadersConfigSUT, "referrerPolicy", "same-origin");
    }

    @Test
    void shouldAddConfiguredSecurityHeaders() {
        // ==================== GIVEN ====================
        // Preparar un mock de intercambio web para una solicitud GET
        MockServerWebExchange mockWebExchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get(
                        "/mcp/stream").build());
        WebFilterChain mockFilterChain = serverWebExchange -> Mono.empty();

        // ==================== WHEN ====================
        // Ejecutar el filtro de seguridad en la solicitud

        // ==================== THEN ====================
        // Verificar que el filtro se ejecuta correctamente y agrega los headers
        StepVerifier.create(securityHeadersConfigSUT.filter(mockWebExchange, mockFilterChain))
                .verifyComplete();

        // Verificar que todos los headers de seguridad configurados están presentes
        var responseHeaders = mockWebExchange.getResponse().getHeaders();
        assertEquals("default-src 'self'", responseHeaders.getFirst("Content-Security-Policy"));
        assertEquals("max-age=1000", responseHeaders.getFirst("Strict-Transport-Security"));
        assertEquals("nosniff", responseHeaders.getFirst("X-Content-Type-Options"));
        assertEquals("no-store", responseHeaders.getFirst("Cache-Control"));
        assertEquals("no-cache", responseHeaders.getFirst("Pragma"));
        assertEquals("same-origin", responseHeaders.getFirst("Referrer-Policy"));
    }
}

