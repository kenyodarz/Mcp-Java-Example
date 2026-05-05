package co.com.bancolombia.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.test.StepVerifier;

class McpSecurityConfigTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConvertJwtClaimsIntoAuthorities() {
        McpSecurityConfig config = new McpSecurityConfig(
                "https://issuer.example.com",
                "client-id",
                "/roles",
                mapper);

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", "jorge", "roles", List.of("MCP.ADMIN", "MCP.TOOL.SIMPSONS")));

        StepVerifier.create(config.jwtAuthenticationConverter().convert(jwt))
                .assertNext(authentication -> {
                    assertTrue(authentication.getAuthorities()
                            .contains(new SimpleGrantedAuthority("ROLE_MCP.ADMIN")));
                    assertTrue(authentication.getAuthorities()
                            .contains(new SimpleGrantedAuthority("ROLE_MCP.TOOL.SIMPSONS")));
                    assertNotNull(authentication.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenClaimCannotBeConverted() {
        McpSecurityConfig config = new McpSecurityConfig(
                "https://issuer.example.com",
                "client-id",
                "/roles",
                mapper);

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", "jorge", "roles", "not-an-array"));

        StepVerifier.create(config.jwtAuthenticationConverter().convert(jwt))
                .assertNext(authentication -> {
                    assertTrue(authentication.getAuthorities().stream()
                            .noneMatch(authority -> authority.getAuthority().startsWith("ROLE_")));
                    assertNotNull(authentication.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildSecurityWebFilterChain() {
        McpSecurityConfig config = new McpSecurityConfig(
                "https://issuer.example.com",
                "client-id",
                "/roles",
                mapper);

        assertNotNull(config.securityWebFilterChain(ServerHttpSecurity.http(),
                mock(ReactiveJwtDecoder.class)));
    }
}

