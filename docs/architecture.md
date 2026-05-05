# Arquitectura

Esta página describe la arquitectura del Code Review MCP Server, sus componentes principales y las
decisiones técnicas.

---

## 🏛️ Clean Architecture

El proyecto sigue el patrón **Clean Architecture de Bancolombia**, que separa el código en capas
concéntricas con dependencias unidireccionales hacia el centro.

```
┌─────────────────────────────────────────────────────────┐
│                    Infrastructure                        │
│  ┌───────────────────────────────────────────────────┐  │
│  │              Entry Points (MCP Server)            │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │           Domain (Use Cases)                │  │  │
│  │  │  ┌───────────────────────────────────────┐  │  │  │
│  │  │  │      Entities (Models)                │  │  │  │
│  │  │  └───────────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  │              Driven Adapters (Azure, AWS)         │  │
│  └───────────────────────────────────────────────────┘  │
│                    Application (Config)                  │
└─────────────────────────────────────────────────────────┘
```

### Capas

#### 1. **Domain** (Núcleo del Negocio)

**Responsabilidad**: Contiene la lógica de negocio pura, independiente de frameworks.

**Componentes**:

- **Entities** (`domain/model`): Modelos de dominio (UserInfo, AnalysisResult)
- **Gateways** (`domain/model/gateways`): Interfaces de puertos de salida
- **Use Cases** (`domain/usecase`): Casos de uso del negocio

**Ejemplo**:

```java
// domain/usecase/GetUserInfoUseCase.java
public class GetUserInfoUseCase {

    private final UserInfoGateway userInfoGateway;

    public Mono<UserInfo> execute(Integer userId) {
        return userInfoGateway.getUserById(userId);
    }
}
```

#### 2. **Infrastructure** (Detalles Técnicos)

**Responsabilidad**: Implementa los detalles técnicos y adaptadores externos.

**Entry Points** (`infrastructure/entry-points/mcp-server`):

- **Tools**: Herramientas MCP ejecutables
- **Resources**: Recursos de datos
- **Prompts**: Plantillas de conversación

**Driven Adapters** (`infrastructure/driven-adapters`):

- **rest-consumer**: Cliente HTTP para Azure DevOps API
- **aws-step-functions**: Cliente para AWS Step Functions
- **aws-s3**: Cliente para almacenamiento S3

#### 3. **Application** (Configuración)

**Responsabilidad**: Ensambla las dependencias y configura la aplicación.

**Componentes**:

- **Configuration Beans**: Inyección de dependencias
- **Application Properties**: Configuración de Spring Boot
- **Main Class**: Punto de entrada de la aplicación

---

## 🔄 Flujo Reactivo

El servidor implementa **programación reactiva** usando **Spring WebFlux** y **Project Reactor**.

### ¿Por qué Reactivo?

- **🚀 No Bloqueante**: Maneja miles de requests concurrentes sin bloquear threads
- **⚡ Eficiencia**: Mejor uso de recursos del sistema
- **🔄 Backpressure**: Control de flujo de datos
- **⏱️ Timeouts**: Manejo automático de operaciones lentas

### Flujo de una Request

```
Cliente MCP
    │
    ▼
[HTTP POST /mcp/stream]
    │
    ▼
[Security Filter - API Key Validation]
    │
    ▼
[@McpTool Method]
    │
    ▼
[Use Case - Mono<T>]
    │
    ▼
[Driven Adapter - WebClient]
    │
    ▼
[External API (Azure DevOps / AWS)]
    │
    ▼
[Response - Mono<Result>]
    │
    ▼
Cliente MCP
```

### Ejemplo de Código Reactivo

```java

@McpTool(name = "analyze_repository")
public Mono<AnalysisResult> analyzeRepository(
        @McpToolParam(required = true) String repositoryName,
        @McpToolParam(required = false) String branch) {

    return validateInput(repositoryName, branch)
            .flatMap(params -> analyzeUseCase.execute(params))
            .timeout(Duration.ofSeconds(30))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
            .onErrorResume(error -> handleError(error));
}
```

---

## 🛠️ Spring AI Integration

El servidor usa **Spring AI 2.0.0-M5** para implementar el protocolo MCP.

### Componentes MCP

#### @McpTool (Herramientas)

Funciones ejecutables que el modelo de IA puede invocar.

```java
@Component
public class HealthTool {
    
    @McpTool(
        name = "health",
        description = "Verifica el estado del servidor"
    )
    public Mono<String> health() {
        return Mono.just("Server is healthy");
    }
}
```

#### @McpResource (Recursos)

Acceso a datos del sistema.

**Resource Estático**:

```java

@McpResource(
        uri = "resource://system/info",
        name = "system-info",
        description = "Información del sistema"
)
public Mono<ReadResourceResult> getSystemInfo() {
    // Retorna información del sistema
}
```

**Resource Template** (con parámetros):

```java
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
```

#### @McpPrompt (Plantillas)

Plantillas de conversación predefinidas.

```java
@McpPrompt(
    name = "greeting",
    description = "Prompt de saludo"
)
public Mono<GetPromptResult> getGreeting(
        @McpArg(name = "name", required = true) String name) {
    
    PromptMessage message = new PromptMessage(
        Role.USER,
        new TextContent("Hola " + name + ", ¿en qué te ayudo?")
    );
    
    return Mono.just(new GetPromptResult("Saludo", List.of(message)));
}
```

---

## 🔐 Seguridad

### API Key Authentication

El servidor implementa autenticación con API Keys almacenadas en H2.

**Flujo de Autenticación**:

```
1. Cliente envía request con header X-API-Key
2. Security Filter extrae API Key
3. Valida formato: {id}.{secret}
4. Busca API Key en base de datos
5. Verifica BCrypt hash del secret
6. Valida que esté activa y no expirada
7. Permite o rechaza request
8. Audita el acceso
```

**Componentes**:

- **ApiKeyAuthenticationFilter**: Filtro de seguridad
- **ApiKeyService**: Validación y gestión de keys
- **AuditService**: Registro de accesos

Para más detalles, consulta la [Guía de Seguridad](security.md).

---

## ☁️ Integración con Azure DevOps

El servidor se conecta a Azure DevOps para leer repositorios.

**Driven Adapter**: `infrastructure/driven-adapters/azure-devops-client`

**Funcionalidades**:

- Listar repositorios
- Leer contenido de archivos
- Navegar estructura de directorios
- Acceder a diferentes ramas

**Ejemplo**:

```java
public Mono<Repository> getRepository(String repoName) {
    return webClient.get()
        .uri("/repositories/{repoName}", repoName)
        .retrieve()
        .bodyToMono(Repository.class)
        .timeout(Duration.ofSeconds(10))
        .retryWhen(Retry.backoff(3, Duration.ofMillis(500)));
}
```

---

## ⚙️ AWS Integration

### Step Functions

**Propósito**: Orquestar análisis de larga duración de forma asíncrona.

**Flujo**:

1. Tool `analyze_repository` inicia ejecución de Step Function
2. Step Function descarga código desde Azure DevOps
3. Invoca AWS Bedrock (Claude 3.5 Sonnet) para análisis
4. Almacena resultados en S3
5. Retorna ARN de ejecución

### S3

**Propósito**: Almacenar reportes de análisis.

**Estructura**:

```
s3://code-review-results/
├── {execution-id}/
│   ├── analysis-report.json
│   ├── architecture-diagram.png
│   └── recommendations.md
```

### Bedrock

**Propósito**: Análisis de código con Claude 3.5 Sonnet.

**Prompt Template**:

```
Analiza el siguiente repositorio Java que implementa Clean Architecture.
Identifica:
1. Violaciones de dependencias entre capas
2. Código duplicado
3. Oportunidades de refactoring
4. Mejores prácticas no aplicadas

Repositorio: {repository_name}
Rama: {branch}
```

---

## 📊 Decisiones Arquitectónicas

### ¿Por qué Clean Architecture?

✅ **Separación de Responsabilidades**: Lógica de negocio independiente de frameworks  
✅ **Testabilidad**: Fácil de probar con mocks  
✅ **Mantenibilidad**: Cambios en infraestructura no afectan el dominio  
✅ **Escalabilidad**: Fácil agregar nuevos adapters

### ¿Por qué Programación Reactiva?

✅ **Eficiencia**: Mejor uso de recursos del servidor  
✅ **Escalabilidad**: Maneja miles de requests concurrentes  
✅ **Resiliencia**: Timeouts, retry, circuit breaker integrados  
✅ **Composición**: Operadores funcionales para flujos complejos

### ¿Por qué Spring AI?

✅ **Estándar**: Implementación oficial del protocolo MCP  
✅ **Integración**: Nativa con Spring Boot ecosystem  
✅ **Anotaciones**: Desarrollo declarativo con @McpTool, @McpResource  
✅ **Soporte**: Mantenido por Spring team

---

## 🔗 Estructura de Directorios

```
mcp/
├── domain/
│   ├── model/
│   │   ├── UserInfo.java
│   │   ├── AnalysisResult.java
│   │   └── gateways/
│   │       ├── UserInfoGateway.java
│   │       └── AnalysisGateway.java
│   └── usecase/
│       ├── GetUserInfoUseCase.java
│       └── AnalyzeRepositoryUseCase.java
│
├── infrastructure/
│   ├── driven-adapters/
│   │   ├── rest-consumer/
│   │   │   └── adapters/
│   │   │       └── AzureDevOpsAdapter.java
│   │   └── aws-step-functions/
│   │       └── StepFunctionsAdapter.java
│   │
│   └── entry-points/
│       └── mcp-server/
│           ├── tools/
│           │   ├── HealthTool.java
│           │   └── ExampleTool.java
│           ├── resources/
│           │   ├── SystemInfoResource.java
│           │   └── UserInfoResource.java
│           └── prompts/
│               └── ExamplePrompt.java
│
└── applications/
    └── app-service/
        ├── src/main/
        │   ├── java/
        │   │   └── config/
        │   │       └── UseCaseConfig.java
        │   └── resources/
        │       └── application.yaml
        └── build.gradle
```

---

## 🧪 Testing Strategy

### Unit Tests

**Objetivo**: Probar lógica de negocio aislada.

```java

@Test
void testGetUserInfoUseCase() {
    // Given
    when(gateway.getUserById(1))
            .thenReturn(Mono.just(new UserInfo(1, "Test")));

    // When
    Mono<UserInfo> result = useCase.execute(1);

    // Then
    StepVerifier.create(result)
            .assertNext(user -> assertThat(user.getName()).isEqualTo("Test"))
            .verifyComplete();
}
```

### Integration Tests

**Objetivo**: Probar integración entre componentes.

```java

@SpringBootTest
@AutoConfigureWebTestClient
class McpServerIntegrationTest {

    @Test
    void testHealthTool() {
        webTestClient.post()
                .uri("/mcp/stream")
                .header("X-API-Key", "dev-client.dev-secret-key-12345")
                .bodyValue(Map.of("method", "tools/call",
                        "params", Map.of("name", "health")))
                .exchange()
                .expectStatus().isOk();
    }
}
```

---

## 📈 Métricas y Observabilidad

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

---

**💡 Tip**: La arquitectura está diseñada para ser extensible. Agregar un nuevo Tool, Resource o
Prompt es tan simple como crear una clase con la anotación correspondiente.

Para más detalles técnicos:

- [API Reference](api-reference.md)
- [Security](security.md)
- [Deployment](deployment.md)
