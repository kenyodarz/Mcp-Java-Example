# POC: Múltiples Servidores MCP con Service Principals Independientes en Entra ID

## 📋 Contexto del Problema

Actualmente el agente consumidor está configurado con:

- **1 Client App Registration** (el agente consumidor)
- **1 OAuth2 Client Registration** (`mcp-server`)
- **Múltiples servidores MCP** (server1, server2) que comparten la misma protección OAuth2

### Limitación Actual

En la configuración actual (`application.yaml`), solo hay una registration de OAuth2:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          mcp-server: # ❌ Un solo registration para todos los MCPs
            provider: "entra-id"
            client-id: "${ENTRA_ID_CLIENT_ID}"
            client-secret: "${ENTRA_ID_CLIENT_SECRET}"
            authorization-grant-type: "client_credentials"
            scope:
              - "api://${ENTRA_ID_CLIENT_ID}/.default"
```

Y en el código (`OAuth2WebClientConfig.java`):

```java
// ❌ Un solo defaultClientRegistrationId para todos los requests
oauth2.setDefaultClientRegistrationId("mcp-server");
```

---

## 🎯 Nuevo Escenario: Múltiples Service Principals

### Arquitectura Propuesta

```
┌─────────────────────────────────────────────────────────────────┐
│                        ENTRA ID TENANT                          │
│                                                                 │
│  ┌──────────────────┐                                           │
│  │   Client App     │  (El Agente Consumidor)                   │
│  │   Registration   │                                           │
│  │                  │                                           │
│  │  Client ID:      │                                           │
│  │  abc123...       │                                           │
│  │  Client Secret   │                                           │
│  └────────┬─────────┘                                           │
│           │                                                     │
│           │ Puede generar tokens para:                          │
│           │                                                     │
│           ├──────────────┬──────────────┬──────────────┐        │
│           ▼              ▼              ▼              ▼        │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────┐  │
│  │ MCP Server │  │ MCP Server │  │ MCP Server │  │ MCP Server│  │
│  │ Finance SP │  │  HR SP     │  │  Sales SP  │  │  Legal SP │  │
│  │            │  │            │  │            │  │           │  │
│  │ API ID:    │  │ API ID:    │  │ API ID:    │  │ API ID:   │  │
│  │ finance123 │  │ hr456      │  │ sales789   │  │ legal012  │  │
│  └────────────┘  └────────────┘  └────────────┘  └───────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

         ▼                ▼                ▼               ▼
    
    MCP Server      MCP Server       MCP Server      MCP Server
    Finance         HR               Sales           Legal
    (Port 8080)     (Port 8081)      (Port 8082)     (Port 8083)
```

### Modelo de Seguridad: 1 Client → N API Apps

En Entra ID, el flujo `client_credentials` permite que **una aplicación cliente pueda obtener tokens
para múltiples APIs**:

- **1 Client App** = El Agente Consumidor
- **N API Apps** = Cada servidor MCP tiene su propio Service Principal (API App Registration)

**Relación de confianza:**

```
Client App (Agente)  ──┬──→  API App 1 (MCP Finance)  →  Token 1
                       ├──→  API App 2 (MCP HR)       →  Token 2
                       ├──→  API App 3 (MCP Sales)    →  Token 3
                       └──→  API App 4 (MCP Legal)    →  Token 4
```

Cada relación genera un token diferente con:

- **Audience específico** (`aud` claim = API App Client ID)
- **Scopes específicos** del API correspondiente
- **Tiempo de vida independiente**

---

## 🔧 Cómo se Reflejaría en el Agente

### 1. Configuración en `application.yaml`

```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            # Servidor MCP de Finanzas
            mcp-finance:
              url: "${MCP_FINANCE_URL:http://localhost:8080}"
              endpoint: "/mcp/stream"

            # Servidor MCP de RRHH
            mcp-hr:
              url: "${MCP_HR_URL:http://localhost:8081}"
              endpoint: "/mcp/stream"

            # Servidor MCP de Ventas
            mcp-sales:
              url: "${MCP_SALES_URL:http://localhost:8082}"
              endpoint: "/mcp/stream"

            # Servidor MCP de Legal
            mcp-legal:
              url: "${MCP_LEGAL_URL:http://localhost:8083}"
              endpoint: "/mcp/stream"

  security:
    oauth2:
      client:
        registration:
          # ✅ Registration para MCP Finance
          mcp-finance:
            provider: "entra-id-finance"
            client-id: "${ENTRA_ID_CLIENT_ID}"  # El mismo Client ID del agente
            client-secret: "${ENTRA_ID_CLIENT_SECRET}"
            authorization-grant-type: "client_credentials"
            scope:
              - "api://${MCP_FINANCE_API_ID}/.default"  # Scope del MCP Finance

          # ✅ Registration para MCP HR
          mcp-hr:
            provider: "entra-id-hr"
            client-id: "${ENTRA_ID_CLIENT_ID}"  # El mismo Client ID del agente
            client-secret: "${ENTRA_ID_CLIENT_SECRET}"
            authorization-grant-type: "client_credentials"
            scope:
              - "api://${MCP_HR_API_ID}/.default"  # Scope del MCP HR

          # ✅ Registration para MCP Sales
          mcp-sales:
            provider: "entra-id-sales"
            client-id: "${ENTRA_ID_CLIENT_ID}"  # El mismo Client ID del agente
            client-secret: "${ENTRA_ID_CLIENT_SECRET}"
            authorization-grant-type: "client_credentials"
            scope:
              - "api://${MCP_SALES_API_ID}/.default"  # Scope del MCP Sales

          # ✅ Registration para MCP Legal
          mcp-legal:
            provider: "entra-id-legal"
            client-id: "${ENTRA_ID_CLIENT_ID}"  # El mismo Client ID del agente
            client-secret: "${ENTRA_ID_CLIENT_SECRET}"
            authorization-grant-type: "client_credentials"
            scope:
              - "api://${MCP_LEGAL_API_ID}/.default"  # Scope del MCP Legal

        provider:
          # Todos usan el mismo tenant pero diferentes token-uris si fuera necesario
          entra-id-finance:
            token-uri: "https://login.microsoftonline.com/${ENTRA_ID_TENANT_ID}/oauth2/v2.0/token"

          entra-id-hr:
            token-uri: "https://login.microsoftonline.com/${ENTRA_ID_TENANT_ID}/oauth2/v2.0/token"

          entra-id-sales:
            token-uri: "https://login.microsoftonline.com/${ENTRA_ID_TENANT_ID}/oauth2/v2.0/token"

          entra-id-legal:
            token-uri: "https://login.microsoftonline.com/${ENTRA_ID_TENANT_ID}/oauth2/v2.0/token"
```

### Variables de Entorno Requeridas

```bash
# Client App (Agente Consumidor) - ÚNICO
ENTRA_ID_CLIENT_ID=abc-123-456-agente-consumidor
ENTRA_ID_CLIENT_SECRET=secret-del-agente
ENTRA_ID_TENANT_ID=tenant-bancolombia-id

# API Apps (Cada MCP Server) - MÚLTIPLES
MCP_FINANCE_API_ID=finance-api-app-id-111
MCP_HR_API_ID=hr-api-app-id-222
MCP_SALES_API_ID=sales-api-app-id-333
MCP_LEGAL_API_ID=legal-api-app-id-444

# URLs de los servidores MCP
MCP_FINANCE_URL=https://mcp-finance.bancolombia.com
MCP_HR_URL=https://mcp-hr.bancolombia.com
MCP_SALES_URL=https://mcp-sales.bancolombia.com
MCP_LEGAL_URL=https://mcp-legal.bancolombia.com
```

---

### 2. Cambios Conceptuales en `OAuth2WebClientConfig.java`

El problema principal es que actualmente el `WebClient.Builder` tiene un **único**
`defaultClientRegistrationId`:

```java
// ❌ ACTUAL: Un solo registration para todos
oauth2.setDefaultClientRegistrationId("mcp-server");
```

#### Opción A: WebClient dinámico por servidor MCP (RECOMENDADA)

Crear múltiples `WebClient.Builder` beans, uno por cada servidor MCP:

```java

@Configuration
public class OAuth2WebClientConfig {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder
                        .builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService);

        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }

    // ✅ WebClient.Builder para MCP Finance
    @Bean("webClientBuilderFinance")
    public WebClient.Builder webClientBuilderFinance(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        return createOAuth2WebClientBuilder(authorizedClientManager, "mcp-finance");
    }

    // ✅ WebClient.Builder para MCP HR
    @Bean("webClientBuilderHR")
    public WebClient.Builder webClientBuilderHR(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        return createOAuth2WebClientBuilder(authorizedClientManager, "mcp-hr");
    }

    // ✅ WebClient.Builder para MCP Sales
    @Bean("webClientBuilderSales")
    public WebClient.Builder webClientBuilderSales(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        return createOAuth2WebClientBuilder(authorizedClientManager, "mcp-sales");
    }

    // ✅ WebClient.Builder para MCP Legal
    @Bean("webClientBuilderLegal")
    public WebClient.Builder webClientBuilderLegal(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        return createOAuth2WebClientBuilder(authorizedClientManager, "mcp-legal");
    }

    // Método helper para crear builders con el registration correcto
    private WebClient.Builder createOAuth2WebClientBuilder(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
            String clientRegistrationId) {

        log.info("=== Configuring WebClient.Builder for: {} ===", clientRegistrationId);

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        authorizedClientManager);

        // ✅ Cada builder tiene su propio registration ID
        oauth2.setDefaultClientRegistrationId(clientRegistrationId);

        WebClient.Builder builder = WebClient.builder()
                .filter(oauth2)
                .filter((request, next) -> {
                    log.debug("[{}] MCP Request: {} {}",
                            clientRegistrationId, request.method(), request.url());
                    return next.exchange(request)
                            .map(response -> normalizeMcpPlainTextAcceptedResponse(
                                    request.method().name(), response))
                            .doOnNext(response -> log.debug("[{}] MCP Response: {}",
                                    clientRegistrationId, response.statusCode()));
                });

        log.info("✅ WebClient.Builder configured for: {}", clientRegistrationId);
        return builder;
    }

    private ClientResponse normalizeMcpPlainTextAcceptedResponse(
            String method, ClientResponse response) {
        var contentType = response.headers().contentType();
        var isCompatibleTextPlain = contentType
                .map(type -> type.isCompatibleWith(MediaType.TEXT_PLAIN))
                .orElse(false);
        var isAcceptedPost = "POST".equals(method) && response.statusCode().is2xxSuccessful();

        if (isAcceptedPost && isCompatibleTextPlain) {
            return response.mutate()
                    .headers(headers -> headers.remove(HttpHeaders.CONTENT_TYPE))
                    .build();
        }
        return response;
    }
}
```

#### Opción B: WebClient con routing dinámico basado en URL

Crear un único `WebClient.Builder` que determine dinámicamente qué `clientRegistrationId` usar
basándose en la URL del request:

```java

@Configuration
public class OAuth2WebClientConfig {

    // Mapeo de URL patterns a clientRegistrationIds
    private static final Map<String, String> URL_TO_CLIENT_REGISTRATION = Map.of(
            "mcp-finance", "mcp-finance",
            "mcp-hr", "mcp-hr",
            "mcp-sales", "mcp-sales",
            "mcp-legal", "mcp-legal",
            "8080", "mcp-finance",    // Por puerto local
            "8081", "mcp-hr",
            "8082", "mcp-sales",
            "8083", "mcp-legal"
    );

    @Bean
    public WebClient.Builder webClientBuilder(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {

        log.info("=== Configuring Dynamic OAuth2 WebClient.Builder ===");

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        authorizedClientManager);

        // ❌ NO setear defaultClientRegistrationId
        // oauth2.setDefaultClientRegistrationId("mcp-server");

        WebClient.Builder builder = WebClient.builder()
                .filter((request, next) -> {
                    // ✅ Determinar dinámicamente el clientRegistrationId
                    String registrationId = determineClientRegistrationId(
                            request.url().toString());

                    log.debug("Routing request to {} using registration: {}",
                            request.url(), registrationId);

                    // Establecer el clientRegistrationId en los atributos del request
                    ClientRequest modifiedRequest = ClientRequest.from(request)
                            .attribute(ServerOAuth2AuthorizedClientExchangeFilterFunction
                                    .CLIENT_REGISTRATION_ID_ATTR_NAME, registrationId)
                            .build();

                    return next.exchange(modifiedRequest);
                })
                .filter(oauth2)  // El filtro OAuth2 usa el atributo establecido arriba
                .filter((request, next) -> {
                    log.debug("MCP Request: {} {}", request.method(), request.url());
                    return next.exchange(request)
                            .map(response -> normalizeMcpPlainTextAcceptedResponse(
                                    request.method().name(), response))
                            .doOnNext(response -> log.debug("MCP Response: {}",
                                    response.statusCode()));
                });

        log.info("✅ Dynamic OAuth2 WebClient.Builder configured");
        return builder;
    }

    private String determineClientRegistrationId(String url) {
        // Buscar en el mapeo qué clientRegistrationId corresponde
        for (Map.Entry<String, String> entry : URL_TO_CLIENT_REGISTRATION.entrySet()) {
            if (url.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Fallback a un default o lanzar excepción
        log.warn("No clientRegistrationId found for URL: {}, using default", url);
        return "mcp-finance";  // O lanzar excepción
    }

    // ... resto del código
}
```

---

### 3. Configuración de MCP Client Adapter

Spring AI MCP necesitaría saber qué `WebClient.Builder` usar para cada conexión:

```java

@Configuration
public class McpClientAdapterConfig {

    /**
     * Conexión al MCP Finance con su WebClient específico
     */
    @Bean
    public McpClient mcpClientFinance(
            @Qualifier("webClientBuilderFinance") WebClient.Builder webClientBuilder) {
        return createMcpClient(
                webClientBuilder,
                "${MCP_FINANCE_URL:http://localhost:8080}",
                "mcp-finance"
        );
    }

    /**
     * Conexión al MCP HR con su WebClient específico
     */
    @Bean
    public McpClient mcpClientHR(
            @Qualifier("webClientBuilderHR") WebClient.Builder webClientBuilder) {
        return createMcpClient(
                webClientBuilder,
                "${MCP_HR_URL:http://localhost:8081}",
                "mcp-hr"
        );
    }

    /**
     * Conexión al MCP Sales con su WebClient específico
     */
    @Bean
    public McpClient mcpClientSales(
            @Qualifier("webClientBuilderSales") WebClient.Builder webClientBuilder) {
        return createMcpClient(
                webClientBuilder,
                "${MCP_SALES_URL:http://localhost:8082}",
                "mcp-sales"
        );
    }

    /**
     * Conexión al MCP Legal con su WebClient específico
     */
    @Bean
    public McpClient mcpClientLegal(
            @Qualifier("webClientBuilderLegal") WebClient.Builder webClientBuilder) {
        return createMcpClient(
                webClientBuilder,
                "${MCP_LEGAL_URL:http://localhost:8083}",
                "mcp-legal"
        );
    }

    private McpClient createMcpClient(
            WebClient.Builder webClientBuilder,
            String serverUrl,
            String serverName) {

        log.info("Creating MCP Client for: {} at {}", serverName, serverUrl);

        // Configuración específica del cliente MCP usando el WebClient correcto
        return StreamableHttpMcpClient.builder()
                .webClient(webClientBuilder.baseUrl(serverUrl).build())
                .endpoint("/mcp/stream")
                .build();
    }
}
```

---

## 🔐 Flujo de Autenticación por Request

### Secuencia cuando el agente llama a un MCP específico:

```
┌──────────┐
│  Agente  │
└────┬─────┘
     │
     │ 1. Invocar herramienta de Finance
     ▼
┌─────────────────────────────────┐
│  ChatClient con MCP Tools       │
└────────┬────────────────────────┘
         │
         │ 2. Enrutar al McpClient correcto
         ▼
┌─────────────────────────────────┐
│  McpClientFinance               │
│  (usa webClientBuilderFinance)  │
└────────┬────────────────────────┘
         │
         │ 3. Filtro OAuth2 intercepta
         ▼
┌─────────────────────────────────────────────────────────┐
│  ServerOAuth2AuthorizedClientExchangeFilterFunction     │
│                                                         │
│  - Busca en cache: ¿Hay token válido para "mcp-finance"?│
│    ├─ SÍ → Usa token cacheado                           │
│    └─ NO → Solicita nuevo token                         │
└────────┬────────────────────────────────────────────────┘
         │
         │ 4. Si no hay token válido
         ▼
┌──────────────────────────────────────────────────┐
│  ReactiveOAuth2AuthorizedClientManager           │
│                                                  │
│  - Obtiene ClientRegistration: "mcp-finance"     │
│  - Client ID: abc-123-456 (agente)               │
│  - Client Secret: ***                            │
│  - Scope: api://finance-api-app-id-111/.default  │
└────────┬─────────────────────────────────────────┘
         │
         │ 5. POST token request
         ▼
┌──────────────────────────────────────────────────┐
│  Entra ID Token Endpoint                         │
│  https://login.microsoftonline.com/.../token     │
│                                                  │
│  Body:                                           │
│    grant_type=client_credentials                 │
│    client_id=abc-123-456                         │
│    client_secret=***                             │
│    scope=api://finance-api-app-id-111/.default   │
└────────┬─────────────────────────────────────────┘
         │
         │ 6. Token response
         ▼
┌──────────────────────────────────────────────────┐
│  Access Token para MCP Finance                   │
│                                                  │
│  {                                               │
│    "access_token": "eyJ0eXAi...",                │
│    "token_type": "Bearer",                       │
│    "expires_in": 3599,                           │
│    "scope": "api://finance-api-app-id-111/..."   │
│  }                                               │
│                                                  │
│  Token JWT decodificado:                         │
│  {                                               │
│    "aud": "finance-api-app-id-111",  ← audience  │
│    "iss": "https://sts.windows.net/.../",        │
│    "appid": "abc-123-456",  ← client del agente  │
│    "roles": ["MCP.User"],                        │
│    "scp": "MCP.Access",                          │
│    "exp": 1234567890                             │
│  }                                               │
└────────┬─────────────────────────────────────────┘
         │
         │ 7. Cache token + enviar request
         ▼
┌──────────────────────────────────────────────────┐
│  HTTP Request al MCP Finance Server              │
│                                                  │
│  POST https://mcp-finance.bancolombia.com/mcp... │
│  Authorization: Bearer eyJ0eXAi...               │
└────────┬─────────────────────────────────────────┘
         │
         │ 8. MCP Finance valida token
         ▼
┌──────────────────────────────────────────────────┐
│  MCP Finance Server                              │
│                                                  │
│  1. Valida firma del token (JWK de Entra ID)     │
│  2. Verifica audience: "finance-api-app-id-111"  │
│  3. Verifica expiración                          │
│  4. Verifica roles/scopes                        │
│  5. ✅ Procesa request                           │
└────────┬─────────────────────────────────────────┘
         │
         │ 9. Response
         ▼
┌──────────────────────────────────────────────────┐
│  Resultado de la herramienta Finance             │
└──────────────────────────────────────────────────┘
```

### Si el siguiente request va a MCP HR:

```
┌──────────┐
│  Agente  │ → Invocar herramienta de HR
└──────────┘
     │
     ▼
McpClientHR (usa webClientBuilderHR)
     │
     ▼
Filtro OAuth2 con registrationId="mcp-hr"
     │
     ▼
Token Manager:
  - Busca token cacheado para "mcp-hr" → NO EXISTE
  - Solicita nuevo token con scope: api://hr-api-app-id-222/.default
     │
     ▼
Entra ID genera OTRO token diferente:
  {
    "aud": "hr-api-app-id-222",  ← audience diferente
    "appid": "abc-123-456",      ← mismo client del agente
    ...
  }
     │
     ▼
Request al MCP HR con token específico de HR
```

---

## 📊 Tabla Comparativa de Tokens

| Request a   | ClientRegistrationId | Scope                                   | Token Audience (`aud`)   | Token Cached Key |
|-------------|----------------------|-----------------------------------------|--------------------------|------------------|
| MCP Finance | `mcp-finance`        | `api://finance-api-app-id-111/.default` | `finance-api-app-id-111` | `mcp-finance`    |
| MCP HR      | `mcp-hr`             | `api://hr-api-app-id-222/.default`      | `hr-api-app-id-222`      | `mcp-hr`         |
| MCP Sales   | `mcp-sales`          | `api://sales-api-app-id-333/.default`   | `sales-api-app-id-333`   | `mcp-sales`      |
| MCP Legal   | `mcp-legal`          | `api://legal-api-app-id-444/.default`   | `legal-api-app-id-444`   | `mcp-legal`      |

**Puntos clave:**

- ✅ Mismo `client_id` y `client_secret` del agente para todos
- ✅ Diferentes `scope` por cada API
- ✅ Diferentes `audience` en cada token
- ✅ Cada token se cachea independientemente
- ✅ Los tokens NO son intercambiables entre MCPs

---

## 🎓 Ventajas de esta Arquitectura

### 1. **Separación de Privilegios**

Cada servidor MCP puede tener sus propios roles y permisos:

```
MCP Finance Token:
  - roles: ["Finance.Read", "Finance.Calculate"]
  - NO tiene acceso a datos de HR

MCP HR Token:
  - roles: ["HR.Read", "HR.Employees"]
  - NO tiene acceso a datos financieros
```

### 2. **Aislamiento de Seguridad**

Si un token se compromete, solo afecta a un MCP específico:

```
🔓 Token de Finance comprometido
  ❌ Puede acceder a MCP Finance
  ✅ NO puede acceder a MCP HR, Sales, Legal
```

### 3. **Auditoría Granular**

En Entra ID se puede auditar exactamente qué agente accedió a qué API:

```
Sign-in Logs:
  - App: Agente Consumidor (abc-123-456)
  - Resource: MCP Finance API (finance-api-app-id-111)
  - Time: 2026-03-09 10:30:00
  - Status: Success

  - App: Agente Consumidor (abc-123-456)
  - Resource: MCP HR API (hr-api-app-id-222)
  - Time: 2026-03-09 10:31:15
  - Status: Success
```

### 4. **Control de Acceso por API**

Cada MCP puede tener diferentes API Permissions en Entra ID:

```
MCP Finance API Permissions:
  - Agente Consumidor: ✅ Granted
  - Agente Analytics: ✅ Granted
  - Agente Support: ❌ Not granted

MCP HR API Permissions:
  - Agente Consumidor: ✅ Granted
  - Agente Analytics: ❌ Not granted
  - Agente Support: ❌ Not granted
```

### 5. **Rotación de Secretos Independiente**

Cada API puede rotar sus secretos sin afectar otras:

```
MCP Finance API:
  - Rota su App Registration secret cada 90 días
  - NO afecta tokens de HR, Sales, Legal

Agente Consumidor:
  - Rota su client secret cada 180 días
  - Afecta a TODOS los MCPs (pero es más controlado)
```

---

## 🚨 Consideraciones de Implementación

### 1. **Cache de Tokens**

Spring Security OAuth2 cachea automáticamente los tokens por `clientRegistrationId`:

```java
// Internamente Spring hace algo así:
Map<String, OAuth2AuthorizedClient> tokenCache = {
                "mcp-finance" ->

OAuth2AuthorizedClient(token:"eyJ...", expiresAt:...),
  "mcp-hr"->

OAuth2AuthorizedClient(token:"eyJ...", expiresAt:...),
  "mcp-sales"->

OAuth2AuthorizedClient(token:"eyJ...", expiresAt:...),
  "mcp-legal"->

OAuth2AuthorizedClient(token:"eyJ...", expiresAt:...)
}
```

### 2. **Refresh Automático**

Cuando un token expira, Spring OAuth2 lo renueva automáticamente:

```
Request a MCP Finance
  ↓
¿Token existe en cache? → SÍ
  ↓
¿Token expirado? → SÍ
  ↓
Solicitar nuevo token automáticamente
  ↓
Actualizar cache
  ↓
Continuar con request
```

### 3. **Rate Limiting en Entra ID**

Ten en cuenta que Entra ID tiene límites:

- Token requests: ~500 req/min por tenant
- Con 4 MCPs y tokens que expiran cada hora:
    - 4 tokens iniciales
    - 4 tokens/hora para refresh
    - Total: ~8 token requests/hora → Muy por debajo del límite

### 4. **Startup Time**

En el primer request a cada MCP, se debe obtener el token:

```
Primer request a Finance: +200ms (obtener token)
Segundo request a Finance: +5ms (token cacheado)

Primer request a HR: +200ms (obtener token)
Segundo request a HR: +5ms (token cacheado)
```

**Solución:** Pre-warm tokens al startup:

```java

@Component
public class TokenPreWarmer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private McpClient mcpClientFinance;

    @Autowired
    private McpClient mcpClientHR;

    // ... otros MCP clients

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Pre-warming OAuth2 tokens for all MCP servers...");

        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> warmupMcpClient(mcpClientFinance, "Finance")),
                CompletableFuture.runAsync(() -> warmupMcpClient(mcpClientHR, "HR")),
                CompletableFuture.runAsync(() -> warmupMcpClient(mcpClientSales, "Sales")),
                CompletableFuture.runAsync(() -> warmupMcpClient(mcpClientLegal, "Legal"))
        ).join();

        log.info("✅ All OAuth2 tokens pre-warmed successfully");
    }

    private void warmupMcpClient(McpClient client, String name) {
        try {
            // Hacer una llamada simple para forzar la obtención del token
            client.listTools().block();
            log.info("✅ Token warmed up for MCP {}", name);
        } catch (Exception e) {
            log.error("❌ Failed to warm up token for MCP {}: {}", name, e.getMessage());
        }
    }
}
```

---

## 📝 Resumen de Cambios Conceptuales

### En `application.yaml`:

```yaml
# ANTES: 1 registration
spring.security.oauth2.client.registration:
  mcp-server: { ... }

# DESPUÉS: N registrations (uno por MCP)
spring.security.oauth2.client.registration:
  mcp-finance: { scope: "api://finance-id/.default" }
  mcp-hr: { scope: "api://hr-id/.default" }
  mcp-sales: { scope: "api://sales-id/.default" }
  mcp-legal: { scope: "api://legal-id/.default" }
```

### En `OAuth2WebClientConfig.java`:

```java
// ANTES: 1 WebClient.Builder con defaultClientRegistrationId fijo
@Bean
public WebClient.Builder webClientBuilder(...) {
    oauth2.setDefaultClientRegistrationId("mcp-server");
}

// DESPUÉS: N WebClient.Builder (uno por MCP)
@Bean("webClientBuilderFinance")
public WebClient.Builder webClientBuilderFinance(...) {
    oauth2.setDefaultClientRegistrationId("mcp-finance");
}

@Bean("webClientBuilderHR")
public WebClient.Builder webClientBuilderHR(...) {
    oauth2.setDefaultClientRegistrationId("mcp-hr");
}
// ... etc
```

### En MCP Client Adapter:

```java
// ANTES: Spring AI auto-configura basado en application.yaml
// (o un único McpClient para todos los servers)

// DESPUÉS: Crear beans explícitos de McpClient
@Bean
public McpClient mcpClientFinance(
        @Qualifier("webClientBuilderFinance") WebClient.Builder builder) {
    return createMcpClient(builder, "mcp-finance-url");
}

@Bean
public McpClient mcpClientHR(
        @Qualifier("webClientBuilderHR") WebClient.Builder builder) {
    return createMcpClient(builder, "mcp-hr-url");
}
// ... etc
```

---

## 🔍 Verificación de la Configuración

### Logs esperados al startup:

```
INFO  OAuth2WebClientConfig - === Configuring OAuth2 Authorized Client Manager (Service-based) ===
INFO  OAuth2WebClientConfig - ✅ OAuth2 client_credentials flow configured (no web context required)

INFO  OAuth2WebClientConfig - === Configuring WebClient.Builder for: mcp-finance ===
INFO  OAuth2WebClientConfig - ✅ WebClient.Builder configured for: mcp-finance

INFO  OAuth2WebClientConfig - === Configuring WebClient.Builder for: mcp-hr ===
INFO  OAuth2WebClientConfig - ✅ WebClient.Builder configured for: mcp-hr

INFO  OAuth2WebClientConfig - === Configuring WebClient.Builder for: mcp-sales ===
INFO  OAuth2WebClientConfig - ✅ WebClient.Builder configured for: mcp-sales

INFO  OAuth2WebClientConfig - === Configuring WebClient.Builder for: mcp-legal ===
INFO  OAuth2WebClientConfig - ✅ WebClient.Builder configured for: mcp-legal

INFO  TokenPreWarmer - Pre-warming OAuth2 tokens for all MCP servers...
DEBUG OAuth2 - Requesting token for client: mcp-finance, scope: api://finance-api-app-id-111/.default
DEBUG OAuth2 - Token obtained successfully for mcp-finance
DEBUG OAuth2 - Requesting token for client: mcp-hr, scope: api://hr-api-app-id-222/.default
DEBUG OAuth2 - Token obtained successfully for mcp-hr
DEBUG OAuth2 - Requesting token for client: mcp-sales, scope: api://sales-api-app-id-333/.default
DEBUG OAuth2 - Token obtained successfully for mcp-sales
DEBUG OAuth2 - Requesting token for client: mcp-legal, scope: api://legal-api-app-id-444/.default
DEBUG OAuth2 - Token obtained successfully for mcp-legal
INFO  TokenPreWarmer - ✅ All OAuth2 tokens pre-warmed successfully

INFO  Application - Started AgentConsumerApplication in 5.234 seconds
```

### Logs durante ejecución:

```
DEBUG OAuth2WebClientConfig - [mcp-finance] MCP Request: POST https://mcp-finance.bancolombia.com/mcp/stream
DEBUG OAuth2 - Using cached token for mcp-finance (expires in 3421s)
DEBUG OAuth2WebClientConfig - [mcp-finance] MCP Response: 200 OK

DEBUG OAuth2WebClientConfig - [mcp-hr] MCP Request: POST https://mcp-hr.bancolombia.com/mcp/stream
DEBUG OAuth2 - Using cached token for mcp-hr (expires in 3418s)
DEBUG OAuth2WebClientConfig - [mcp-hr] MCP Response: 200 OK
```

---

## 🎯 Conclusión

Esta arquitectura permite que el **Agente Consumidor** (1 Client App) se comunique de forma segura
con **múltiples servidores MCP**, cada uno protegido por su **propio Service Principal** en Entra
ID, mediante:

1. **Múltiples OAuth2 Client Registrations** - Una por cada MCP
2. **Múltiples WebClient.Builder beans** - Cada uno con su `clientRegistrationId`
3. **Múltiples MCP Client beans** - Cada uno usa el WebClient correcto
4. **Gestión automática de tokens** - Spring OAuth2 cachea y renueva automáticamente
5. **Aislamiento de seguridad** - Cada token solo es válido para su MCP específico

El resultado es una solución **escalable**, **segura** y **mantenible** que aprovecha las
capacidades nativas de Spring Security OAuth2 para manejar múltiples APIs protegidas desde un único
cliente.

---

## 📚 Referencias

- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Microsoft Entra ID Client Credentials Flow](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-client-creds-grant-flow)
- [Spring AI MCP Documentation](https://docs.spring.io/spring-ai/reference/)
- [Reactive OAuth2 Authorized Client Manager](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/oauth2/client/ReactiveOAuth2AuthorizedClientManager.html)

---

**Documento creado para:** Prueba de Concepto (POC)  
**Fecha:** 2026-03-09  
**Propósito:** Arquitectura de seguridad con múltiples Service Principals en Entra ID para
servidores MCP

