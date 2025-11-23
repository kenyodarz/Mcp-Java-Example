# Estructura de Módulos - Clean Architecture Bancolombia

## Ubicación de Archivos de Configuración

### 📁 applications/app-service/src/main/java/co/com/bancolombia/config/

```
applications/app-service/src/main/java/co/com/bancolombia/config/
├── SpringAiMcpConfiguration.java      ← ✅ NUEVA CLASE AQUÍ
├── UseCasesConfig.java                ← Ya existe
└── WebFluxConfig.java                 ← Opcional para configuraciones adicionales
```

### 📋 Razón

Según Clean Architecture de Bancolombia:

- **applications/app-service**: Es la capa más externa
- Responsable de ensamblar módulos y resolver dependencias
- Contiene toda la configuración de Spring (@Configuration)
- Aquí se configuran los beans de infraestructura

---

## 📁 infrastructure/entry-points/mcp-server/

Este módulo contiene la **implementación** de los componentes MCP:

```
infrastructure/entry-points/mcp-server/src/main/java/co/com/bancolombia/mcp/
├── config/
│   ├── McpToolsConfig.java            ← Configuración de Tools
│   ├── McpResourcesConfig.java        ← Configuración de Resources
│   └── McpPromptsConfig.java          ← Configuración de Prompts
├── tools/
│   ├── SaludoTool.java
│   └── HealthTool.java
├── resources/
│   ├── SystemInfoResource.java
│   └── UserInfoResource.java
└── prompts/
    ├── SaludoPrompt.java
    └── BienvenidaPrompt.java
```

---

## 🎯 Responsabilidades por Capa

### Domain (Núcleo)

```
domain/
├── model/          ← Entidades de dominio (UserInfo, etc.)
└── usecase/        ← Lógica de negocio (GetUserInfoUseCase)
```

### Infrastructure (Detalles de Implementación)

```
infrastructure/
├── driven-adapters/        ← Adaptadores de salida (APIs externas, BD)
│   └── rest-consumer/
└── entry-points/           ← Adaptadores de entrada (REST, MCP)
    └── mcp-server/
```

### Applications (Ensamblaje)

```
applications/
└── app-service/
    ├── config/             ← ✅ Todas las @Configuration
    └── MainApplication.java
```

---

## 📝 Ejemplo Correcto: SpringAiMcpConfiguration.java

**Ubicación**:
`applications/app-service/src/main/java/co/com/bancolombia/config/SpringAiMcpConfiguration.java`

```java
package co.com.bancolombia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@ComponentScan(basePackages = {
        "co.com.bancolombia.mcp.tools",
        "co.com.bancolombia.mcp.resources",
        "co.com.bancolombia.mcp.prompts",
        "co.com.bancolombia.mcp.config"  // ← Escanea las configs de MCP
})
public class SpringAiMcpConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        log.info("Configurando ObjectMapper para Spring AI MCP");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return mapper;
    }
}
```

---

## 🔄 Flujo de Dependencias

```
MainApplication.java (app-service)
    ↓ importa
SpringAiMcpConfiguration.java (app-service/config)
    ↓ escanea componentes
McpToolsConfig.java (mcp-server/config)
    ↓ inyecta
SaludoTool.java (mcp-server/tools)
    ↓ usa
GetUserInfoUseCase.java (domain/usecase)
    ↓ depende de
UserInfoGateway.java (domain/model/gateways)
    ↓ implementado por
SimpsonsApiAdapter.java (rest-consumer/adapters)
```

---

## ⚠️ Errores Comunes a Evitar

❌ **NO colocar @Configuration en infrastructure**

```
infrastructure/entry-points/mcp-server/config/SpringAiMcpConfiguration.java  ← ❌ INCORRECTO
```

✅ **SÍ colocar @Configuration en applications**

```
applications/app-service/src/main/java/co/com/bancolombia/config/SpringAiMcpConfiguration.java  ← ✅ CORRECTO
```

❌ **NO colocar @Component en applications**

```java
// En applications/app-service
@Component  ← ❌
Los componentes
van en
infrastructure

public class SaludoTool {

}
```

✅ **SÍ colocar @Component en infrastructure**

```java
// En infrastructure/entry-points/mcp-server
@Component  ← ✅CORRECTO

public class SaludoTool {

}
```

---

## 📦 Resumen de Ubicaciones

| Tipo de Clase            | Módulo          | Paquete                                |
|--------------------------|-----------------|----------------------------------------|
| `@Configuration`         | `app-service`   | `co.com.bancolombia.config`            |
| `@Component` (Tools)     | `mcp-server`    | `co.com.bancolombia.mcp.tools`         |
| `@Component` (Resources) | `mcp-server`    | `co.com.bancolombia.mcp.resources`     |
| `@Component` (Prompts)   | `mcp-server`    | `co.com.bancolombia.mcp.prompts`       |
| Use Cases                | `usecase`       | `co.com.bancolombia.usecase`           |
| Entities/Models          | `model`         | `co.com.bancolombia.model`             |
| Adapters                 | `rest-consumer` | `co.com.bancolombia.consumer.adapters` |

---

## 🚀 Comandos para Crear la Estructura

```bash
# Desde la raíz del proyecto

# Crear SpringAiMcpConfiguration en app-service
touch applications/app-service/src/main/java/co/com/bancolombia/config/SpringAiMcpConfiguration.java

# Verificar que las configuraciones de MCP existan
ls -la infrastructure/entry-points/mcp-server/src/main/java/co/com/bancolombia/mcp/config/
```

---

## 📖 Referencias

- [Clean Architecture - Bancolombia](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)
- [Scaffold Generator](https://github.com/bancolombia/scaffold-clean-architecture)
- [Documentación interna del proyecto](README.md)