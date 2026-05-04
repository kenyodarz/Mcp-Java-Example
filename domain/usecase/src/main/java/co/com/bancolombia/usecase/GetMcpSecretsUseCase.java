package co.com.bancolombia.usecase;

import co.com.bancolombia.model.gateway.AsyncSecretsGateway;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * UseCase para obtener credenciales del MCP (Client-ID y Secret) de forma reactiva.
 * <p>
 * Orquesta llamadas al secretsGateway para obtener múltiples secretos necesarios para autenticar y
 * autorizar invocaciones MCP.
 */
@RequiredArgsConstructor
public class GetMcpSecretsUseCase {

    private static final String MCP_CLIENT_ID_SECRET = "mcp-client-id";
    private static final String MCP_CLIENT_SECRET_SECRET = "mcp-client-secret";
    private final AsyncSecretsGateway secretsGateway;

    /**
     * Obtiene las credenciales del MCP (Client-ID y Secret) de forma reactiva.
     *
     * @return Mono<Map> con las claves "clientId" y "clientSecret"
     */
    public Mono<Map<String, String>> execute() {
        Mono<String> clientIdMono = secretsGateway.getSecret(MCP_CLIENT_ID_SECRET);
        Mono<String> clientSecretMono = secretsGateway.getSecret(MCP_CLIENT_SECRET_SECRET);

        return Mono.zip(clientIdMono, clientSecretMono)
                .map(tuple -> {
                    Map<String, String> credentials = new HashMap<>();
                    credentials.put("clientId", tuple.getT1());
                    credentials.put("clientSecret", tuple.getT2());
                    return credentials;
                });
    }
}

