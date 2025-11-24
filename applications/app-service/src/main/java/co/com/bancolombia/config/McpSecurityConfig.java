package co.com.bancolombia.config;

import co.com.bancolombia.security.apikey.InMemoryCachedApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class McpSecurityConfig {

    private final InMemoryCachedApiKeyRepository cachedRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configurando MCP Security 100% reactivo con caché de API Keys");

        // Creamos un filtro de autenticación personalizado
        AuthenticationWebFilter apiKeyFilter = new AuthenticationWebFilter(authenticationManager());
        apiKeyFilter.setServerAuthenticationConverter(apiKeyAuthenticationConverter());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/h2-console/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(apiKeyFilter,
                        org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    // Este es el AuthenticationManager reactivo que valida la clave
    private ReactiveAuthenticationManager authenticationManager() {
        return authentication -> {
            String apiKey = (String) authentication.getCredentials();
            return cachedRepository.findByKeyId(apiKey)
                    .flatMap(entity -> {
                        if (entity != null && entity.enabled()) {
                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                    entity.id(), null, java.util.Collections.emptyList());
                            return Mono.just(auth);
                        }
                        return Mono.empty(); // clave inválida
                    })
                    .switchIfEmpty(Mono.defer(Mono::empty));
        };
    }

    // Convierte header → Authentication (sin validar aún)
    private org.springframework.security.web.server.authentication.ServerAuthenticationConverter apiKeyAuthenticationConverter() {
        return exchange -> {
            String header = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (header == null || header.isBlank()) {
                header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (header != null && header.startsWith("Bearer ")) {
                    header = header.substring(7);
                }
            }

            if (header == null || header.isBlank()) {
                return Mono.empty();
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(header, header);
            return Mono.just(auth);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}