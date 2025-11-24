package co.com.bancolombia.model.apikey.gateways;

/**
 * Gateway para operaciones de encoding/hashing de passwords
 * <p>
 * Esta abstracción permite que el dominio no dependa de frameworks específicos como Spring
 * Security, BCrypt, etc.
 * <p>
 * La implementación concreta estará en la capa de infraestructura.
 */
public interface PasswordEncoderGateway {

    /**
     * Verifica si un password en texto plano coincide con un hash
     *
     * @param rawPassword     Password en texto plano
     * @param encodedPassword Password hasheado
     * @return true si coinciden, false en caso contrario
     */
    boolean matches(String rawPassword, String encodedPassword);

    /**
     * Genera un hash a partir de un password en texto plano
     *
     * @param rawPassword Password en texto plano
     * @return Hash del password
     */
    String encode(String rawPassword);
}