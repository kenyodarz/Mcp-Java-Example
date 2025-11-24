package co.com.bancolombia.security.password;

import co.com.bancolombia.model.apikey.gateways.PasswordEncoderGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa PasswordEncoderGateway usando BCrypt de Spring Security
 * <p>
 * Esta clase pertenece a la infraestructura porque depende de frameworks externos. El dominio solo
 * conoce la interfaz PasswordEncoderGateway, no esta implementación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BCryptPasswordEncoderAdapter implements PasswordEncoderGateway {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        log.debug("🔐 Verificando password");

        if (rawPassword == null || encodedPassword == null) {
            log.warn("⚠️ Password o hash nulo");
            return false;
        }

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.debug(matches ? "✅ Password correcto" : "❌ Password incorrecto");

        return matches;
    }

    @Override
    public String encode(String rawPassword) {
        log.debug("🔐 Generando hash de password");

        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password no puede estar vacío");
        }

        return passwordEncoder.encode(rawPassword);
    }
}