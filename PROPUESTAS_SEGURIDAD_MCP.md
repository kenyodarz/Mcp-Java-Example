# Propuestas de Seguridad para MCP Server

## 🎯 Objetivo

Implementar seguridad en el servidor MCP usando **Spring AI MCP Security** (Work In Progress) para
proteger herramientas, recursos y prompts.

---

## 📊 Comparativa de Opciones

| Opción               | Complejidad | Producción Ready | Bancolombia Friendly | Mejor Para                       |
|----------------------|-------------|------------------|----------------------|----------------------------------|
| **1. API Key**       | ⭐ Baja      | ✅ Sí             | ✅✅ Sí                | Demos, POCs, ambiente controlado |
| **2. OAuth2 + JWT**  | ⭐⭐⭐ Alta    | ⚠️ WIP           | ✅ Sí                 | Producción real                  |
| **3. Híbrido**       | ⭐⭐ Media    | ⚠️ WIP           | ✅✅ Sí                | Producción gradual               |
| **4. Sin Seguridad** | ⭐ Muy Baja  | ❌ No             | ❌ No                 | Solo desarrollo local            |

---

## 🔐 Opción 1: API Key Authentication (RECOMENDADA PARA EMPEZAR)

### Descripción

Autenticación simple basada en **API Keys** enviadas en headers HTTP. Similar a como funcionan
muchas APIs públicas.

### Ventajas

✅ **Implementación rápida** (2-3 horas)  
✅ **Fácil de entender y mantener**  
✅ **Compatible con todos los clientes MCP**  
✅ **No requiere infrastructure adicional** (no auth server)  
✅ **Perfecto para POCs y demos**  
✅ **Alineado con prácticas de Bancolombia** (API Key Management)

### Desventajas

⚠️ No tiene expiración automática de tokens  
⚠️ Menos granular que OAuth2  
⚠️ Requiere rotación manual de keys

### Implementación

#### 1. Dependencias

```gradle
// infrastructure/entry-points/mcp-server/build.gradle
dependencies {
    // ... dependencias existentes

    // Spring Security
implementation '
org.springframework.boot:spring-boot-starter-security'

// MCP Server Security (WIP)
implementation 'org.springaicommunity:mcp-server-security'
}
```

#### 2. Configuración de Seguridad

```java
// applications/app-service/src/.../config/SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/actuator/health").permitAll()  // Health check público
                                .anyRequest().authenticated()
                        // Todo lo demás requiere auth
                )
                .with(mcpServerApiKey(), apiKey -> {
                    apiKey.apiKeyRepository(apiKeyRepository());
                    apiKey.headerName("X-API-Key"); // Nombre del header
                })
                .build();
    }

    @Bean
    public ApiKeyEntityRepository<ApiKeyEntityImpl> apiKeyRepository() {
        // API Keys para diferentes clientes/ambientes
        List<ApiKeyEntityImpl> apiKeys = List.of(
                // Desarrollo
                ApiKeyEntityImpl.builder()
                        .id("dev-client")
                        .name("Cliente de Desarrollo")
                        .secret("dev-secret-key-12345")
                        .enabled(true)
                        .build(),

                // QA
                ApiKeyEntityImpl.builder()
                        .id("qa-client")
                        .name("Cliente de QA")
                        .secret("qa-secret-key-67890")
                        .enabled(true)
                        .build(),

                // Claude Desktop
                ApiKeyEntityImpl.builder()
                        .id("claude-desktop")
                        .name("Claude Desktop App")
                        .secret("claude-secret-key-abcde")
                        .enabled(true)
                        .build()
        );

        return new InMemoryApiKeyEntityRepository<>(apiKeys);
    }
}
```

#### 3. Uso desde el cliente

```bash
# Llamada con curl
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{"method": "tools/list"}'
```

#### 4. Configuración en Claude Desktop

```json
// ~/.config/claude/config.json
{
  "mcpServers": {
    "bancolombia-mcp": {
      "url": "http://localhost:8080/mcp/stream",
      "headers": {
        "X-API-Key": "claude-desktop.claude-secret-key-abcde"
      }
    }
  }
}
```

### Mejoras Productivas

Para producción, implementar:

1. **Repository en Base de Datos**:

```java

@Repository
public class DatabaseApiKeyRepository implements ApiKeyEntityRepository<ApiKeyEntityImpl> {
    // Implementación con JPA/R2DBC
}
```

2. **Rotación de API Keys**:

```java

@Scheduled(cron = "0 0 0 * * *") // Diariamente
public void rotateExpiredKeys() {
    // Lógica de rotación
}
```

3. **Logs de Auditoría**:

```java

@Aspect
@Component
public class ApiKeyAuditAspect {

    @Around("@annotation(org.springframework.ai.mcp.server.annotation.McpTool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) {
        String apiKeyId = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        log.info("Tool called by API Key: {}", apiKeyId);
        // ... más auditoría
    }
}
```

---

## 🔑 Opción 2: OAuth2 + JWT (PRODUCCIÓN ENTERPRISE)

### Descripción

Autenticación robusta basada en **OAuth 2.0** con **JWT tokens** y un **Authorization Server**
dedicado.

### Ventajas

✅ **Estándar de la industria**  
✅ **Tokens con expiración automática**  
✅ **Granularidad fina de permisos** (scopes)  
✅ **Soporte para refresh tokens**  
✅ **Auditoría completa**  
✅ **Integración con IAM corporativo**

### Desventajas

⚠️ **Complejidad alta** (1-2 semanas)  
⚠️ **Requiere Authorization Server** (Spring Authorization Server)  
⚠️ **Configuración más compleja** en clientes  
⚠️ **WIP en Spring AI MCP Security** (puede cambiar)

### Arquitectura

```
┌─────────────┐        ┌──────────────────┐        ┌─────────────┐
│   Cliente   │───1───→│  Authorization   │←───2───│  MCP Server │
│  (Claude)   │        │     Server       │        │ (Resource   │
│             │←──3────│  (Spring Auth)   │        │  Server)    │
└─────────────┘        └──────────────────┘        └─────────────┘
      │                                                     ▲
      └────────────────── 4. API Call + JWT ──────────────┘
```

### Implementación

#### 1. Dependencias

```gradle
// Agregar en app-service/build.gradle
dependencies {
implementation '
org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
implementation 'org.springaicommunity:mcp-server-security'
}
```

#### 2. Configuración del Resource Server (MCP Server)

```java
// applications/app-service/src/.../config/OAuth2SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class OAuth2SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/mcp").permitAll()  // initialize y list públicos
                        .anyRequest().authenticated()
                )
                .with(McpServerOAuth2Configurer.mcpServerOAuth2(), oauth2 -> {
                    oauth2.authorizationServer(issuerUri);
                    oauth2.validateAudienceClaim(true); // Validar 'aud' claim
                })
                .build();
    }
}
```

#### 3. Configuración

```yaml
# application.yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000  # URL del Auth Server
          audiences: mcp-bancolombia          # Validar audience
```

#### 4. Proteger Tools Específicos

```java

@Component
public class SecuredTool {

    @PreAuthorize("hasAuthority('SCOPE_mcp:tools')")
    @McpTool(name = "securedTool", description = "Tool protegido")
    public Mono<String> execute(@McpToolParam String param) {
        // Acceder a la autenticación
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return Mono.just("Ejecutado por: " + username);
    }
}
```

### Crear el Authorization Server

**NOTA**: Esto requiere un proyecto separado. Ver ejemplo completo en:
https://github.com/spring-ai-community/mcp-security/tree/main/samples

---

## 🔄 Opción 3: Híbrido (API Key + OAuth2) - RECOMENDADA PARA PRODUCCIÓN GRADUAL

### Descripción

Combinar **API Keys** para operaciones básicas y **OAuth2** para operaciones sensibles.

### Ventajas

✅ **Migración gradual** (empezar simple, crecer a enterprise)  
✅ **Flexibilidad**: Diferentes clientes, diferentes auth  
✅ **Balance**: Simplicidad + Seguridad  
✅ **Compatible con roadmap de Bancolombia**

### Estrategia

1. **Fase 1 (1-2 semanas)**: API Keys
    - Implementar API Key authentication
    - Proteger endpoints básicos
    - Desplegar en DEV/QA

2. **Fase 2 (2-3 semanas)**: Agregar OAuth2
    - Implementar Authorization Server
    - Migrar gradualmente a OAuth2
    - API Keys como fallback

3. **Fase 3 (1 semana)**: Deprecar API Keys
    - Solo OAuth2 en producción
    - API Keys solo en desarrollo

### Implementación

```java

@Configuration
@EnableWebSecurity
public class HybridSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                // OAuth2 JWT
                .with(McpServerOAuth2Configurer.mcpServerOAuth2(), oauth2 -> {
                    oauth2.authorizationServer(issuerUri);
                })
                // API Key como fallback
                .with(mcpServerApiKey(), apiKey -> {
                    apiKey.apiKeyRepository(apiKeyRepository());
                })
                .build();
    }
}
```

---

## 📋 Comparativa Detallada

### Esfuerzo de Implementación

| Actividad             | API Key   | OAuth2      | Híbrido  |
|-----------------------|-----------|-------------|----------|
| Setup inicial         | 2-3 horas | 1-2 semanas | 3-4 días |
| Configuración cliente | 5 min     | 30 min      | 10 min   |
| Integración IAM       | N/A       | 2-3 días    | 2-3 días |
| Testing               | 1 día     | 1 semana    | 1 semana |
| Documentación         | 1 día     | 3 días      | 2 días   |

### Casos de Uso Recomendados

| Escenario                         | Recomendación                   |
|-----------------------------------|---------------------------------|
| POC/Demo interno                  | **API Key**                     |
| Desarrollo local                  | **Sin seguridad** o **API Key** |
| QA/UAT                            | **API Key**                     |
| Producción (MVP)                  | **API Key** con DB              |
| Producción (Enterprise)           | **OAuth2**                      |
| Migración gradual                 | **Híbrido**                     |
| Integración con Apps corporativas | **OAuth2**                      |
| Clientes externos (partners)      | **API Key** o **OAuth2**        |

---

## 🎯 Mi Recomendación

### Para tu proyecto actual:

**Empezar con Opción 1 (API Key)** por estas razones:

1. ✅ **Rápida implementación** - Puedes tenerlo funcionando hoy
2. ✅ **Fácil de demostrar** - Stakeholders ven valor inmediato
3. ✅ **Compatible con fase posterior** - Puedes migrar a OAuth2 después
4. ✅ **Suficiente para ambientes no productivos** - DEV/QA/UAT
5. ✅ **Alineado con WIP de Spring AI** - La librería está madura para API Keys

### Roadmap sugerido:

```
Semana 1-2:  API Key Authentication ✅
Semana 3-4:  API Key + DB Repository ✅
Semana 5-6:  OAuth2 Authorization Server (paralelo)
Semana 7+:  Solo OAuth2 en producción
```

---
