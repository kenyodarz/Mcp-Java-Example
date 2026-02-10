package co.com.bancolombia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SecurityHeadersConfig implements WebFilter {

    // Encabezados de seguridad
    private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String PRAGMA = "Pragma";
    private static final String REFERRER_POLICY = "Referrer-Policy";

    // Valores configurables
    @Value("${security.headers.content-security-policy:default-src 'self'; frame-ancestors 'self'; form-action 'self'}")
    private String contentSecurityPolicy;

    @Value("${security.headers.strict-transport-security:max-age=31536000;}")
    private String strictTransportSecurity;

    @Value("${security.headers.x-content-type-options:nosniff}")
    private String xContentTypeOptions;

    @Value("${security.headers.cache-control:no-store}")
    private String cacheControl;

    @Value("${security.headers.pragma:no-cache}")
    private String pragmaValue;

    @Value("${security.headers.referrer-policy:strict-origin-when-cross-origin}")
    private String referrerPolicy;

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();

        // Establecer los encabezados de seguridad
        headers.set(CONTENT_SECURITY_POLICY, contentSecurityPolicy);
        headers.set(STRICT_TRANSPORT_SECURITY, strictTransportSecurity);
        headers.set(X_CONTENT_TYPE_OPTIONS, xContentTypeOptions);
        headers.set(CACHE_CONTROL, cacheControl);
        headers.set(PRAGMA, pragmaValue);
        headers.set(REFERRER_POLICY, referrerPolicy);

        return chain.filter(exchange);
    }
}
