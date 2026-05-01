# Guía de Uso: Plugin MCP para Scaffold Clean Architecture

**Versión:** 3.27.0+  
**Spring AI:** 1.1.0  
**Tipo:** Servidor MCP Reactivo (ASYNC)

---

## 📋 Tabla de Contenidos

1. [Instalación del Plugin](#instalación-del-plugin)
2. [Crear un Proyecto con MCP](#crear-un-proyecto-con-mcp)
3. [Generar Entry Point MCP](#generar-entry-point-mcp)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Desarrollo de Componentes MCP](#desarrollo-de-componentes-mcp)
6. [Configuración](#configuración)
7. [Testing](#testing)
8. [Integración con Claude Desktop](#integración-con-claude-desktop)
9. [Troubleshooting](#troubleshooting)

---

## 1. Instalación del Plugin

### Opción A: Desde Maven Local (Desarrollo)

```bash
# 1. Clonar el repositorio del plugin
git clone https://github.com/bancolombia/scaffold-clean-architecture.git
cd scaffold-clean-architecture

# 2. Compilar y publicar localmente
gradle clean build publishToMavenLocal

# 3. Verificar instalación
ls ~/.m2/repository/co/com/bancolombia/scaffold-clean-architecture/
```

### Opción B: Desde Gradle Plugin Portal (Producción)

```gradle
// build.gradle
plugins {
id '
co.com.bancolombia.cleanArchitecture' version '3.27.0'
}
```

---

## 2. Crear un Proyecto con MCP

### Paso 1: Generar Proyecto Base

```bash
# Crear nuevo proyecto
gradle cleanArchitecture \
  --package=co.com.bancolombia \
  --name=my-mcp-server \
  --type=reactive

cd my-mcp-server
```

### Paso 2: Generar Entry Point MCP

```bash
# Generar MCP con todas las capabilities
gradle generateEntryPoint --type=mcp

# O personalizado
gradle generateEntryPoint --type=mcp \
  --name=MyMcpServer \
  --enable-tools=true \
  --enable-resources=true \
  --enable-prompts=true
```

---

## 3. Generar Entry Point MCP

### Comando Básico

```bash
gradle generateEntryPoint --type=mcp
```

### Parámetros Disponibles

| Parámetro            | Valores        | Default | Descripción             |
|----------------------|----------------|---------|-------------------------|
| `--name`             | String         | `null`  | Nombre del servidor MCP |
| `--enable-tools`     | `true`/`false` | `true`  | Habilitar Tools         |
| `--enable-resources` | `true`/`false` | `true`  | Habilitar Resources     |
| `--enable-prompts`   | `true`/`false` | `true`  | Habilitar Prompts       |

### Ejemplos

```bash
# Solo Tools
gradle generateEntryPoint --type=mcp \
  --enable-tools=true \
  --enable-resources=false \
  --enable-prompts=false

# Solo Resources
gradle generateEntryPoint --type=mcp \
  --enable-tools=false \
  --enable-resources=true \
  --enable-prompts=false

# Nombre personalizado
gradle generateEntryPoint --type=mcp \
  --name=BancolombiaAssistant
```

---

## 4. Estructura del Proyecto

### Estructura Generada

```
my-mcp-server/
├── applications/
│   └── app-service/
│       └── src/main/resources/
│           └── application.yaml  ← Configuración MCP agregada
├── infrastructure/
│   └── entry-points/
│       └── mcp-server/
│           ├── build.gradle
│           └── src/
│               ├── main/java/co/com/bancolombia/mcp/
│               │   ├── tools/
│               │   │   ├── HealthTool.java
│               │   │   └── ExampleTool.java
│               │   ├── resources/
│               │   │   ├── SystemInfoResource.java
│               │   │   └── UserInfoResource.java
│               │   └── prompts/
│               │       └── ExamplePrompt.java
│               └── test/java/co/com/bancolombia/mcp/
│                   ├── tools/
│                   ├── resources/
│                   └── prompts/
└── settings.gradle  ← mcp-server agregado
```

### Archivos Generados

**6 Clases Java:**

1. `HealthTool.java` - Health check tool
2. `ExampleTool.java` - Tool de ejemplo (echo, add)
3. `SystemInfoResource.java` - Resource de información del sistema
4. `UserInfoResource.java` - Resource template con parámetros
5. `ExamplePrompt.java` - Prompt templates
6. `build.gradle` - Dependencias del módulo

**5 Tests:**

1. `HealthToolTest.java`
2. `ExampleToolTest.java`
3. `SystemInfoResourceTest.java`
4. `UserInfoResourceTest.java`
5. `ExamplePromptTest.java`

---

## 5. Desarrollo de Componentes MCP

### 5.1 Crear un Tool

```java
package co.com.bancolombia.mcp.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatorTool {

    @McpTool(
            name = "multiply",
            description = "Multiplica dos números")
    public Mono<Integer> multiply(
            @McpToolParam(description = "Primer número", required = true) int a,
            @McpToolParam(description = "Segundo número", required = true) int b) {
        log.info("Multiplicando {} x {}", a, b);
        return Mono.just(a * b);
    }
}
```

### 5.2 Crear un Resource

```java
package co.com.bancolombia.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ConfigResource {

    private final ObjectMapper objectMapper;

    @McpResource(
            uri = "resource://config/app",
            name = "app-config",
            description = "Configuración de la aplicación")
    public Mono<ReadResourceResult> getConfig() {
        return Mono.fromCallable(() -> {
            Map<String, Object> config = Map.of(
                    "environment", "production",
                    "version", "1.0.0",
                    "features", List.of("mcp", "reactive")
            );

            return new ReadResourceResult(
                    List.of(new TextResourceContents(
                            "resource://config/app",
                            MediaType.APPLICATION_JSON_VALUE,
                            toJson(config)
                    ))
            );
        });
    }

    @SneakyThrows
    private String toJson(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(object);
    }
}
```

### 5.3 Crear un Resource con Parámetros (Template)

```java
package co.com.bancolombia.mcp.resources;

import co.com.bancolombia.usecase.GetProductUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductResource {

    private final ObjectMapper objectMapper;
    private final GetProductUseCase getProductUseCase;

    @McpResource(
            uri = "resource://products/{productId}",
            name = "product-info",
            description = "Información de un producto")
    public Mono<ReadResourceResult> getProduct(String productId) {
        log.info("Obteniendo producto: {}", productId);

        return parseProductId(productId)
                .flatMap(getProductUseCase::execute)
                .map(product -> new ReadResourceResult(
                        List.of(new TextResourceContents(
                                "resource://products/" + productId,
                                MediaType.APPLICATION_JSON_VALUE,
                                toJson(product)
                        ))
                ))
                .onErrorResume(error -> {
                    log.error("Error obteniendo producto {}: {}", productId, error.getMessage());
                    return Mono.just(createErrorResult(productId, error));
                });
    }

    private Mono<Integer> parseProductId(String productId) {
        return Mono.fromCallable(() -> Integer.parseInt(productId));
    }

    private ReadResourceResult createErrorResult(String productId, Throwable error) {
        Map<String, Object> errorResponse = Map.of(
                "error", true,
                "message", error.getMessage(),
                "productId", productId
        );

        return new ReadResourceResult(
                List.of(new TextResourceContents(
                        "resource://products/" + productId,
                        MediaType.APPLICATION_JSON_VALUE,
                        toJson(errorResponse)
                ))
        );
    }

    @SneakyThrows
    private String toJson(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(object);
    }
}
```

### 5.4 Crear un Prompt

```java
package co.com.bancolombia.mcp.prompts;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SupportPrompt {

    @McpPrompt(
            name = "customer-support",
            description = "Genera un prompt de soporte al cliente")
    public Mono<GetPromptResult> customerSupport(
            @McpArg(name = "issue", required = true) String issue,
            @McpArg(name = "priority", required = false) String priority) {

        return Mono.fromCallable(() -> {
            String priorityText = (priority != null) ? priority : "normal";

            String promptText = String.format(
                    "Eres un agente de soporte de Bancolombia. "
                            + "Un cliente reporta el siguiente problema: %s. "
                            + "Prioridad: %s. "
                            + "Proporciona una respuesta profesional y útil.",
                    issue, priorityText
            );

            PromptMessage message = new PromptMessage(
                    Role.USER,
                    new TextContent(promptText)
            );

            return new GetPromptResult("Soporte al cliente", List.of(message));
        });
    }
}
```

---

## 6. Configuración

### application.yaml (Generado Automáticamente)

```yaml
spring:
  application:
    name: my-mcp-server

  ai:
    mcp:
      server:
        protocol: "STATELESS"
        name: "${spring.application.name}"
        version: "1.0.0"
        type: "ASYNC"
        instructions: |
          Servidor MCP reactivo de Bancolombia con capacidades de:
          - Tools: Herramientas ejecutables
          - Resources: Acceso a datos del sistema y usuarios
          - Prompts: Plantillas de conversación personalizadas

          Seguridad: Requiere autenticación via API Key (Header: X-API-Key)

        streamable-http:
          mcp-endpoint: "/mcp/${spring.application.name}"

        capabilities:
          tool: true
          resource: true
          prompt: true
          completion: false

        request-timeout: "30s"
```

### Personalizar Configuración

```yaml
# Cambiar timeout
spring.ai.mcp.server.request-timeout: "60s"

# Cambiar endpoint
spring.ai.mcp.server.streamable-http.mcp-endpoint: "/api/mcp"

# Deshabilitar capabilities
spring.ai.mcp.server.capabilities.prompt: false
```

---

## 7. Testing

### Test de Tool

```java
package co.com.bancolombia.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CalculatorToolTest {

    private CalculatorTool calculatorTool;

    @BeforeEach
    void setUp() {
        calculatorTool = new CalculatorTool();
    }

    @Test
    void testMultiply() {
        Mono<Integer> result = calculatorTool.multiply(5, 3);

        StepVerifier.create(result)
                .expectNext(15)
                .verifyComplete();
    }
}
```

### Test de Resource

```java
package co.com.bancolombia.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class ConfigResourceTest {

    private ConfigResource configResource;

    @BeforeEach
    void setUp() {
        configResource = new ConfigResource(new ObjectMapper());
    }

    @Test
    void testGetConfig() {
        StepVerifier.create(configResource.getConfig())
                .assertNext(result -> {
                    ResourceContents content = result.contents().get(0);
                    assert content instanceof TextResourceContents;

                    TextResourceContents text = (TextResourceContents) content;
                    assert text.uri().equals("resource://config/app");
                    assert text.text().contains("environment");
                })
                .verifyComplete();
    }
}
```

### Ejecutar Tests

```bash
# Todos los tests
gradle test

# Solo tests de MCP
gradle :mcp-server:test

# Con reporte
gradle test jacocoTestReport
```

---

## 8. Integración con Claude Desktop

### Configuración de Claude Desktop

1. **Ubicar archivo de configuración:**
    - **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
    - **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
    - **Linux:** `~/.config/Claude/claude_desktop_config.json`

2. **Agregar servidor MCP:**

```json
{
  "mcpServers": {
    "bancolombia-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "C:/path/to/my-mcp-server/applications/app-service/build/libs/app-service.jar"
      ],
      "env": {
        "SPRING_PROFILES_ACTIVE": "mcp"
      }
    }
  }
}
```

3. **Reiniciar Claude Desktop**

### Verificar Conexión

En Claude Desktop, deberías ver:

- 🔧 Tools disponibles (HealthTool, ExampleTool, etc.)
- 📄 Resources disponibles (SystemInfo, UserInfo, etc.)
- 💬 Prompts disponibles (Greeting, Help, etc.)

---

## 9. Troubleshooting

### Error: Template not found

```bash
# Limpiar cache de Gradle
gradle --stop
Remove-Item -Recurse -Force .gradle\configuration-cache
gradle clean
```

### Error: Cannot resolve method 'text()'

**Problema:** No estás haciendo casting a `TextResourceContents`

**Solución:**

```java
// ❌ Incorrecto
String text = result.contents().get(0).text();

// ✅ Correcto
ResourceContents content = result.contents().get(0);
TextResourceContents text = (TextResourceContents) content;
String json = text.text();
```

### Error: Build path errors

**Solución:** Compilar y publicar el plugin localmente

```bash
cd scaffold-clean-architecture
gradle clean build publishToMavenLocal
```

### Claude Desktop no detecta el servidor

1. **Verificar que el JAR se compiló:**
   ```bash
   gradle :app-service:bootJar
   ls applications/app-service/build/libs/
   ```

2. **Verificar logs de Claude Desktop:**
    - Windows: `%APPDATA%\Claude\logs\`
    - macOS: `~/Library/Logs/Claude/`

3. **Probar el servidor manualmente:**
   ```bash
   java -jar applications/app-service/build/libs/app-service.jar
   ```

---

## 📚 Recursos Adicionales

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol Spec](https://modelcontextprotocol.io/)
- [Bancolombia Clean Architecture](https://github.com/bancolombia/scaffold-clean-architecture)

---

## ✅ Checklist de Desarrollo

- [ ] Plugin instalado localmente
- [ ] Proyecto generado con `cleanArchitecture`
- [ ] Entry point MCP generado
- [ ] Tools personalizados creados
- [ ] Resources con use cases integrados
- [ ] Prompts configurados
- [ ] Tests pasando
- [ ] Configuración YAML ajustada
- [ ] JAR compilado
- [ ] Claude Desktop configurado
- [ ] Servidor funcionando

---

**¡Listo para desarrollar tu servidor MCP reactivo con Bancolombia!** 🚀
