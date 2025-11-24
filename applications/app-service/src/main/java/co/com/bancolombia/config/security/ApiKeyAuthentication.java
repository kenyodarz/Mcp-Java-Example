package co.com.bancolombia.config.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record ApiKeyAuthentication(String apiKey, boolean enabled) implements Authentication {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return enabled ? List.of(new SimpleGrantedAuthority("ROLE_MCP_CLIENT")) : List.of();
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }

    @Override
    public boolean isAuthenticated() {
        return enabled;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return apiKey;
    }
}