package co.com.bancolombia.config.security;

import co.com.bancolombia.security.apikey.InMemoryCachedApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApiKeyWebFilter implements WebFilter {

    private final InMemoryCachedApiKeyRepository cachedRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .cast(ApiKeyAuthentication.class)
                .flatMap(auth -> {
                    if (!auth.isAuthenticated()) {
                        return Mono.error(
                                new BadCredentialsException("Invalid or disabled API Key"));
                    }
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                })
                .switchIfEmpty(
                        chain.filter(exchange)); // Si no hay principal, sigue (otros filtros)
    }
}