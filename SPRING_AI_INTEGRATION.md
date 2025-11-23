# Integración con Spring AI 1.1.0

## Descripción

Este proyecto implementa un servidor MCP (Model Context Protocol) usando Spring AI 1.1.0,
proporcionando una arquitectura reactiva con WebFlux para herramientas, recursos y prompts.

## Componentes Principales

### 1. Tools (Herramientas)

Los tools son funciones ejecutables que el modelo de IA puede invocar:

- **SaludoTool**: Genera saludos personalizados
- **HealthTool**: Verifica el estado del servidor

```java
@Component
public class SaludoTool {
    public McpStatelessServerFeatures.AsyncToolSpecification getToolSpecification() {
        // Definición del tool
    }
}
```

### 2. Resources (Recursos)

Los resources proporcionan acceso a datos:

- **SystemInfoResource**: Información del sistema
- **UserInfoResource**: Información de usuarios (con template URI)

```java
@Component
public class UserInfoResource {
    public AsyncResourceTemplateSpecification getResourceSpecification() {
        // Definición del resource con URI template
    }
}
```

### 3. Prompts (Plantillas)

Los prompts son plantillas de conversación predefinidas:

- **SaludoPrompt**: Saludo personalizable
- **BienvenidaPrompt**: Bienvenida formal

```java
@Component
public class SaludoPrompt {
    public McpStatelessServerFeatures.AsyncPromptSpecification getPromptSpecification() {
        // Definición del prompt
    }
}
```

## Configuración

### application.yaml

```yaml
spring:
  ai:
    mcp:
      server:
        protocol: "STATELESS"
        name: "mcp-bancolombia"
        type: "ASYNC"
        capabilities:
          tool: true
          resource: true
          prompt: true
```

## Endpoints

### Streaming MCP

```
POST http://localhost:8080/mcp/stream
Content-Type: application/json

{
  "method": "tools/list"
}
```

### Health Check

```
GET http://localhost:8080/actuator/health
```

## Uso de Tools

### Listar Tools Disponibles

```json
{
  "method": "tools/list"
}
```

### Llamar un Tool

```json
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

## Uso de Resources

### Listar Resources

```json
{
  "method": "resources/list"
}
```

### Leer un Resource

```json
{
  "method": "resources/read",
  "params": {
    "uri": "resource://users/1"
  }
}
```

## Uso de Prompts

### Listar Prompts

```json
{
  "method": "prompts/list"
}
```

### Obtener un Prompt

```json
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

## Características Avanzadas

### Manejo de Errores

Todos los componentes implementan manejo robusto de errores:

```java
.onErrorResume(error ->{
        log.

error("Error procesando request",error);
    return Mono.

just(errorResponse);
})
```

### Retry con Backoff

Los resources implementan reintentos automáticos:

```java
.retryWhen(Retry.backoff(3, Duration.ofMillis(500))
        .

maxBackoff(Duration.ofSeconds(2))
        )
```

### Timeouts

Configuración de timeouts para prevenir bloqueos:

```java
.timeout(Duration.ofSeconds(10))
```

### Circuit Breaker

Integración con Resilience4j:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      simpsonsApi:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

## Testing

### Test de Tool

```java
@Test
void testSaludoTool() {
    McpStatelessServerFeatures.AsyncToolSpecification spec = 
        saludoTool.getToolSpecification();
    
    StepVerifier.create(spec.callHandler().apply(null, Map.of("name", "Test")))
        .assertNext(result -> {
            assertThat(result.isError()).isFalse();
        })
        .verifyComplete();
}
```

### Test de Resource

```java
@Test
void testUserInfoResource() {
    StepVerifier.create(getUserInfo(1))
        .assertNext(result -> {
            assertThat(result.contents()).isNotEmpty();
        })
        .verifyComplete();
}
```

## Métricas y Monitoreo

### Actuator Endpoints

- `/actuator/health`: Estado del servidor
- `/actuator/metrics`: Métricas del sistema
- `/actuator/prometheus`: Métricas en formato Prometheus

### Logs

El proyecto utiliza SLF4J con niveles configurables:

```yaml
logging:
  level:
    io.modelcontextprotocol: DEBUG
    org.springframework.ai.mcp: DEBUG
    co.com.bancolombia: DEBUG
```

## Arquitectura Limpia

El proyecto sigue Clean Architecture:

```
domain/
  ├── model/           # Entidades de dominio
  └── usecase/         # Casos de uso
infrastructure/
  ├── driven-adapters/ # Adaptadores de salida
  └── entry-points/    # Puntos de entrada (MCP Server)
applications/
  └── app-service/     # Configuración y arranque
```

## Mejores Prácticas

1. **Programación Reactiva**: Todo el flujo es no bloqueante
2. **Separación de Responsabilidades**: Cada componente tiene una única responsabilidad
3. **Manejo de Errores**: Estrategia consistente en todos los componentes
4. **Logging**: Información detallada para debugging
5. **Testing**: Cobertura de pruebas unitarias e integración
6. **Documentación**: Código autodocumentado con Javadoc

## Recursos Adicionales

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Protocol Specification](https://spec.modelcontextprotocol.io/)
- [Reactor Documentation](https://projectreactor.io/docs)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

## Contribución

Para contribuir al proyecto:

1. Fork el repositorio
2. Crea una rama para tu feature
3. Implementa los cambios siguiendo Clean Architecture
4. Añade tests
5. Crea un Pull Request

## Contacto

Para dudas o sugerencias, contacta al equipo de Ingeniería de Software de Bancolombia.