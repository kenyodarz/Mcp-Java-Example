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

    // ==================== TEST DOUBLES ====================
    // Mapper para procesar claims de JWT
    private final ObjectMapper objectMapperForJwtProcessing = new ObjectMapper();

    @Test
    void shouldConvertJwtClaimsIntoAuthorities() {
        // ==================== GIVEN ====================
        // Preparar la configuración de seguridad MCP
        McpSecurityConfig mcpSecurityConfigSUT = new McpSecurityConfig(
                "https://issuer.example.com",
                "client-id",
                "/roles",
                objectMapperForJwtProcessing);

        // Preparar un JWT con roles MCP
        Jwt jwtTokenWithMcpRoles = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", "jorge", "roles", List.of("MCP.ADMIN", "MCP.TOOL.SIMPSONS")));

        // ==================== WHEN ====================
        // Convertir el JWT en un objeto Authentication con autoridades

        // ==================== THEN ====================
        // Verificar que los roles se convierten correctamente a autoridades
        StepVerifier.create(
                        mcpSecurityConfigSUT.jwtAuthenticationConverter().convert(jwtTokenWithMcpRoles))
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
        // ==================== GIVEN ====================
        // Preparar la configuración de seguridad MCP
        McpSecurityConfig mcpSecurityConfigSUT = new McpSecurityConfig(
                "https://issuer.example.com",
                "client-id",
                "/roles",
                objectMapperForJwtProcessing);

        // Preparar un JWT con un claim 'roles' que no es un array (caso de error)
        Jwt jwtTokenWithInvalidRolesFormat = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", "jorge", "roles", "not-an-array"));

        // ==================== WHEN ====================
        // Intentar convertir el JWT con formato de roles inválido

        // ==================== THEN ====================
        // Verificar que no se generan autoridades con prefijo ROLE_ pero sí retorna el usuario
        StepVerifier.create(mcpSecurityConfigSUT.jwtAuthenticationConverter()
                        .convert(jwtTokenWithInvalidRolesFormat))
                .assertNext(authentication -> {
                    assertTrue(authentication.getAuthorities().stream()
                            .noneMatch(authority -> authority.getAuthority() != null
                                    && authority.getAuthority().startsWith("ROLE_")));
                    assertNotNull(authentication.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildSecurityWebFilterChain() {
        // ==================== GIVEN ====================
        // Preparar la configuración de seguridad MCP
        McpSecurityConfig mcpSecurityConfigSUT = new McpSecurityConfig(
                "https://issuer.example.com",
                "client-id",
                "/roles",
                objectMapperForJwtProcessing);

        // ==================== WHEN ====================
        // Construir la cadena de filtros de seguridad web

        // ==================== THEN ====================
        // Verificar que la cadena de filtros se construye correctamente
        assertNotNull(mcpSecurityConfigSUT.securityWebFilterChain(ServerHttpSecurity.http(),
                mock(ReactiveJwtDecoder.class)));
    }
}

