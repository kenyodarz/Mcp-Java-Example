package co.com.bancolombia.model.apikey;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad de dominio que representa una API Key para autenticación MCP
 * <p>
 * Esta entidad está en el dominio porque representa una regla de negocio: cómo se identifican y
 * autorizan los clientes del sistema.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    /**
     * ID único de la API Key (ej: "dev-client", "prod-app-1")
     */
    private String id;

    /**
     * Nombre descriptivo del cliente
     */
    private String name;

    /**
     * Hash del secret (NUNCA almacenar en texto plano) Se usa BCrypt para el hashing
     */
    private String secretHash;

    /**
     * Indica si la key está activa
     */
    private Boolean enabled;

    /**
     * Fecha de creación
     */
    private LocalDateTime createdAt;

    /**
     * Fecha de última actualización
     */
    private LocalDateTime updatedAt;

    /**
     * Fecha de expiración (null = no expira)
     */
    private LocalDateTime expiresAt;

    /**
     * Último uso de la API Key
     */
    private LocalDateTime lastUsedAt;

    /**
     * Contador de usos
     */
    private Long usageCount;

    /**
     * Descripción o notas adicionales
     */
    private String description;

    /**
     * IP whitelisted (opcional, null = cualquier IP)
     */
    private String allowedIp;

    /**
     * Verifica si la API Key está activa y no ha expirado
     */
    public boolean isValid() {
        if (!Boolean.TRUE.equals(enabled)) {
            return false;
        }

        return expiresAt == null || !LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Verifica si la API Key está próxima a expirar (30 días)
     */
    public boolean isExpiringSoon() {
        if (expiresAt == null) {
            return false;
        }

        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        return expiresAt.isBefore(thirtyDaysFromNow);
    }
}