package co.com.bancolombia.security.apikey;

import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntity;
import org.springframework.security.core.CredentialsContainer;

public class InMemoryApiKeyEntity implements ApiKeyEntity, CredentialsContainer {

    private final String id;
    private final String secret;
    private final boolean enabled;

    public InMemoryApiKeyEntity(String id, String secret, boolean enabled) {
        this.id = id;
        this.secret = secret;
        this.enabled = enabled;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        // No-op, solo por contrato
    }

    @Override
    public InMemoryApiKeyEntity copy() {
        return new InMemoryApiKeyEntity(id, secret, enabled);
    }
}
