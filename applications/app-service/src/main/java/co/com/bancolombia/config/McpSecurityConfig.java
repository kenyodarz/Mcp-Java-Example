package co.com.bancolombia.config;

import co.com.bancolombia.security.apikey.InMemoryCachedApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.security.server.config.McpApiKeyConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class McpSecurityConfig {

    private final InMemoryCachedApiKeyRepository cachedRepository;

    public McpSecurityConfig(InMemoryCachedApiKeyRepository cachedRepository) {
        this.cachedRepository = cachedRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("Configurando MCP con API Keys en caché (oficial 2025 - versión final)");

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                // ESTA ES LA ÚNICA FORMA QUE FUNCIONA CON TU VERSIÓN
                .with(McpApiKeyConfigurer.mcpServerApiKey(), customizer -> customizer
                        .apiKeyRepository(cachedRepository)
                        .headerName("X-API-Key")
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}