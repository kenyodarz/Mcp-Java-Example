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

    private SecurityHeadersConfig config;

    @BeforeEach
    void setUp() {
        config = new SecurityHeadersConfig();
        ReflectionTestUtils.setField(config, "contentSecurityPolicy", "default-src 'self'");
        ReflectionTestUtils.setField(config, "strictTransportSecurity", "max-age=1000");
        ReflectionTestUtils.setField(config, "xContentTypeOptions", "nosniff");
        ReflectionTestUtils.setField(config, "cacheControl", "no-store");
        ReflectionTestUtils.setField(config, "pragmaValue", "no-cache");
        ReflectionTestUtils.setField(config, "referrerPolicy", "same-origin");
    }

    @Test
    void shouldAddConfiguredSecurityHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get(
                        "/mcp/stream").build());
        WebFilterChain chain = serverWebExchange -> Mono.empty();

        StepVerifier.create(config.filter(exchange, chain))
                .verifyComplete();

        var headers = exchange.getResponse().getHeaders();
        assertEquals("default-src 'self'", headers.getFirst("Content-Security-Policy"));
        assertEquals("max-age=1000", headers.getFirst("Strict-Transport-Security"));
        assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"));
        assertEquals("no-store", headers.getFirst("Cache-Control"));
        assertEquals("no-cache", headers.getFirst("Pragma"));
        assertEquals("same-origin", headers.getFirst("Referrer-Policy"));
    }
}

