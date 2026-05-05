# Integración con Spring AI 2.0.0-M5

## Descripción

Este proyecto implementa un **servidor MCP (Model Context Protocol)** usando **Spring AI 2.0.0-M5**,
proporcionando una arquitectura reactiva con WebFlux para herramientas, recursos y prompts.

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────┐
│           Cliente MCP (Claude, Cursor, etc.)        │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP/SSE
                   ▼
┌─────────────────────────────────────────────────────┐
│            MCP Server (Spring AI 2.0.0-M5)          │
│  ┌───────────────────────────────────────────────┐  │
│  │  Entry Point: /mcp/stream (STATELESS/ASYNC)  │  │
│  └───────────────────────────────────────────────┘  │
│                                                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │   @McpTool  │  │ @McpResource │  │ @McpPrompt │ │
│  └─────────────┘  └──────────────┘  └────────────┘ │
│         │                │                  │        │
│         └────────────────┴──────────────────┘        │
│                          │                           │
│                          ▼                           │
│              ┌───────────────────────┐               │
│              │   Domain Use Cases    │               │
│              └───────────────────────┘               │
│                          │                           │
│                          ▼                           │
│              ┌───────────────────────┐               │
│              │  Driven Adapters      │               │
│              │  (REST, DB, etc.)     │               │
│              └───────────────────────┘               │
└─────────────────────────────────────────────────────┘
```

## 📦 Componentes Principales

### 1. Tools (Herramientas)

Los tools son **funciones ejecutables** que el modelo de IA puede invocar usando la anotación
`@McpTool`.

**Características:**

- Métodos reactivos con `Mono<T>` para servidores ASYNC
- Validación automática de parámetros con `@McpToolParam`
- Generación automática de JSON Schema

**Ejemplo:**

```java
@Component
public class SaludoTool {

    @McpTool(
            name = "saludoTool",
            description = "Genera un saludo personalizado"
    )
    public Mono<String> saludo(
            @McpToolParam(description = "Nombre de la persona", required = true)
            String name
    ) {
        return Mono.fromCallable(() ->
                "¡Hola " + name + "! Bienvenido al servidor MCP de Bancolombia."
        );
    }
}
```

### 2. Resources (Recursos)

Los resources proporcionan **acceso a datos** usando la anotación `@McpResource`.

**Tipos:**

- **Resource estático**: URI fija (ej: `resource://system/info`)
- **Resource template**: URI con parámetros (ej: `resource://users/{userId}`)

**Ejemplo estático:**

```java

@Component
public class SystemInfoResource {

    @McpResource(
            uri = "resource://system/info",
            name = "system-info",
            description = "Información del sistema"
    )
    public Mono<ReadResourceResult> getSystemInfo() {
        return Mono.fromCallable(() -> {
            // Retornar información del sistema
        });
    }
}
```

**Ejemplo con template:**

```java
@Component
public class UserInfoResource {

    @McpResource(
            uri = "resource://users/{userId}",
            name = "user-info",
            description = "Información de usuario"
    )
    public Mono<ReadResourceResult> getUserInfo(String userId) {
        return parseUserId(userId)
                .flatMap(id -> getUserInfoUseCase.execute(id))
                .map(this::createResourceResult);
    }
}
```

### 3. Prompts (Plantillas)

Los prompts son **plantillas de conversación** predefinidas usando la anotación `@McpPrompt`.

**Ejemplo:**

```java
@Component
public class SaludoPrompt {

    @McpPrompt(
            name = "saludo",
            description = "Prompt de saludo personalizable"
    )
    public Mono<GetPromptResult> getSaludoPrompt(
            @McpArg(name = "nombre", required = true) String nombre
    ) {
        return Mono.fromCallable(() -> {
            PromptMessage message = new PromptMessage(
                    Role.USER,
                    new TextContent("Hola " + nombre + ", ¿en qué te ayudo?")
            );
            return new GetPromptResult("Saludo base", List.of(message));
        });
    }
}
```

## ⚙️ Configuración

### application.yaml

```yaml
server:
  port: 8080

spring:
  application:
    name: "mcp-bancolombia"

  ai:
    mcp:
      server:
        protocol: "STATELESS"           # STATELESS o STREAMABLE
        name: "mcp-bancolombia"
        version: "1.0.0"
        type: "ASYNC"                   # ASYNC para métodos reactivos
        instructions: |
          Servidor MCP reactivo con capacidades de:
          - Tools: Herramientas ejecutables
          - Resources: Acceso a datos
          - Prompts: Plantillas de conversación

        streamable-http:
          mcp-endpoint: "/mcp/stream"

        capabilities:
          tool: true
          resource: true
          prompt: true
          completion: false

        request-timeout: "30s"
```

## 🔌 Endpoints

### Streaming MCP

**URL**: `POST http://localhost:8080/mcp/stream`  
**Content-Type**: `application/json`

### Health Check

**URL**: `GET http://localhost:8080/actuator/health`

## 📡 Uso del MCP Server

### Listar Tools Disponibles

```json
POST /mcp/stream
{
  "method": "tools/list"
}
```

### Llamar un Tool

```json
POST /mcp/stream
{
  "method": "tools/call",
  "params": {
    "name": "saludoTool",
    "arguments": {
      "name": "Jorge"
    }
  }
}
```

### Listar Resources

```json
POST /mcp/stream
{
  "method": "resources/list"
}
```

### Leer un Resource

```json
POST /mcp/stream
{
  "method": "resources/read",
  "params": {
    "uri": "resource://users/1"
  }
}
```

### Listar Prompts

```json
POST /mcp/stream
{
  "method": "prompts/list"
}
```

### Obtener un Prompt

```json
POST /mcp/stream
{
  "method": "prompts/get",
  "params": {
    "name": "saludo",
    "arguments": {
      "nombre": "Jorge"
    }
  }
}
```

## 🚀 Características Avanzadas

### 1. Programación Reactiva

Todo el flujo es **no bloqueante** usando Project Reactor:

```java
return getUserInfoUseCase.execute(userId)
    .

map(userInfo ->

createResult(userInfo))
        .

timeout(Duration.ofSeconds(10))
        .

onErrorResume(error ->

handleError(error));
```

### 2. Manejo de Errores

Estrategia consistente en todos los componentes:

```java
.onErrorResume(error ->{
        log.

error("Error procesando request",error);
    return Mono.

just(createErrorResponse(error));
})
```

### 3. Retry con Backoff

Los resources implementan reintentos automáticos:

```java
.retryWhen(Retry.backoff(3, Duration.ofMillis(500))
        .

maxBackoff(Duration.ofSeconds(2))
        .

filter(throwable ->!(throwable instanceof IllegalArgumentException))
        )
```

### 4. Timeouts

Prevenir bloqueos indefinidos:

```java
.timeout(Duration.ofSeconds(10))
```

### 5. Circuit Breaker

Integración con Resilience4j:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      simpsonsApi:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

## 🧪 Testing

### Test de Tool

```java
@Test
void testSaludoTool() {
    StepVerifier.create(saludoTool.saludo("Test"))
        .assertNext(result -> {
            assertThat(result).contains("Hola Test");
        })
        .verifyComplete();
}
```

### Test de Resource

```java
@Test
void testUserInfoResource() {
    StepVerifier.create(userInfoResource.getUserInfo("1"))
        .assertNext(result -> {
            assertThat(result.contents()).isNotEmpty();
        })
        .verifyComplete();
}
```

## 📊 Métricas y Monitoreo

### Actuator Endpoints

- `/actuator/health`: Estado del servidor
- `/actuator/metrics`: Métricas del sistema
- `/actuator/prometheus`: Métricas en formato Prometheus
- `/actuator/info`: Información de la aplicación

### Logs Estructurados

```yaml
logging:
  level:
    io.modelcontextprotocol: DEBUG
    org.springframework.ai.mcp: DEBUG
    co.com.bancolombia: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## 🏛️ Arquitectura Limpia

El proyecto sigue **Clean Architecture de Bancolombia**:

```
domain/
  ├── model/           # Entidades de dominio (UserInfo)
  │   └── gateways/    # Interfaces de puertos de salida
  └── usecase/         # Casos de uso (GetUserInfoUseCase)

infrastructure/
  ├── driven-adapters/ # Implementación de gateways
  │   └── rest-consumer/
  │       └── adapters/
  └── entry-points/    # Puntos de entrada
      └── mcp-server/
          ├── tools/
          ├── resources/
          └── prompts/

applications/
  └── app-service/     # Configuración y arranque
      └── config/      # Beans de configuración
```

## ⚡ Mejores Prácticas Implementadas

1. ✅ **Programación Reactiva**: Flujo 100% no bloqueante
2. ✅ **Separación de Responsabilidades**: Clean Architecture
3. ✅ **Manejo de Errores**: Estrategia consistente
4. ✅ **Logging**: Información detallada para debugging
5. ✅ **Testing**: Cobertura de pruebas unitarias
6. ✅ **Documentación**: Código autodocumentado con Javadoc
7. ✅ **Observabilidad**: Métricas y health checks
8. ✅ **Resiliencia**: Circuit breaker, retry, timeouts

## 🔗 Recursos Adicionales

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI MCP Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server.html)
- [MCP Protocol Specification](https://spec.modelcontextprotocol.io/)
- [Project Reactor](https://projectreactor.io/docs)
- [Clean Architecture - Bancolombia](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

## 🤝 Contribución

Para contribuir al proyecto:

1. Fork el repositorio
2. Crea una rama para tu feature: `git checkout -b feature/nueva-funcionalidad`
3. Implementa los cambios siguiendo Clean Architecture
4. Añade tests unitarios
5. Documenta con Javadoc
6. Crea un Pull Request

## 📝 Convenciones de Código

- **Clases**: PascalCase
- **Métodos**: camelCase
- **Constantes**: UPPER_SNAKE_CASE
- **Packages**: lowercase
- **Tests**: Terminar con `Test` o `IT`

## 🐛 Troubleshooting

### Error: "Sync providers doesn't support reactive return types"

**Causa**: Métodos anotados con `@McpTool`, `@McpResource` o `@McpPrompt` no retornan tipos
reactivos.

**Solución**: Cambiar el tipo de retorno a `Mono<T>`:

```java
// ❌ Incorrecto (para servidores ASYNC)
public String saludo(String name) { ...}

// ✅ Correcto
public Mono<String> saludo(String name) {
    return Mono.fromCallable(() -> ...);
}
```

### Error: "No tool/resource/prompt methods found"

**Causa**: Las anotaciones MCP no están siendo detectadas.

**Solución**: Verificar que las clases estén anotadas con `@Component` y que el escaneo de
componentes incluya el paquete correcto.