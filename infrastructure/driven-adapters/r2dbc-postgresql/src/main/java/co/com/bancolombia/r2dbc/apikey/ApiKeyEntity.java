package co.com.bancolombia.r2dbc.apikey;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad de base de datos para API Keys (R2DBC)
 * <p>
 * Esta clase pertenece a la capa de infraestructura y representa el modelo de persistencia (no el
 * modelo de dominio).
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("api_keys")
public class ApiKeyEntity {

    @Id
    @Column("id")
    private String id;

    @Column("name")
    private String name;

    @Column("secret_hash")
    private String secretHash;

    @Column("enabled")
    private Boolean enabled;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    @Column("last_used_at")
    private LocalDateTime lastUsedAt;

    @Column("usage_count")
    private Long usageCount;

    @Column("description")
    private String description;

    @Column("allowed_ip")
    private String allowedIp;
}