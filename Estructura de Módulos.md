# Estructura de Módulos - Clean Architecture Bancolombia + Spring AI MCP

## 📋 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Estructura de Directorios](#estructura-de-directorios)
3. [Responsabilidades por Capa](#responsabilidades-por-capa)
4. [Flujo de Dependencias](#flujo-de-dependencias)
5. [Ubicación de Archivos](#ubicación-de-archivos)
6. [Convenciones](#convenciones)

---

## 🎯 Visión General

Este proyecto combina:

- **Clean Architecture** (Robert C. Martin)
- **Scaffold de Bancolombia** (estructura modular con Gradle)
- **Spring AI MCP 1.1.0** (Model Context Protocol)
- **Programación Reactiva** (Project Reactor)

### Principios Fundamentales

```
🔵 Domain (Núcleo) → No depende de nadie
    ↑
🟢 Infrastructure → Depende del Domain
    ↑
🟡 Applications → Ensambla todo
```

---

## 📁 Estructura de Directorios

```
mcp/
├── domain/                                # 🔵 CAPA DE DOMINIO
│   ├── model/                             # Entidades y puertos
│   │   ├── src/main/java/.../model/
│   │   │   └── userinfo/
│   │   │       ├── UserInfo.java          # Entidad de dominio
│   │   │       └── gateways/
│   │   │           └── UserInfoGateway.java  # Puerto de salida
│   │   └── build.gradle
│   │
│   └── usecase/                           # Casos de uso (lógica de negocio)
│       ├── src/main/java/.../usecase/
│       │   └── GetUserInfoUseCase.java    # Caso de uso
│       └── build.gradle
│
├── infrastructure/                        # 🟢 CAPA DE INFRAESTRUCTURA
│   │
│   ├── driven-adapters/                   # Adaptadores de SALIDA
│   │   └── rest-consumer/                 # Consumidor de APIs REST
│   │       ├── src/main/java/.../consumer/
│   │       │   ├── RestConsumer.java      # Cliente HTTP
│   │       │   ├── adapters/
│   │       │   │   └── SimpsonsApiAdapter.java  # Implementa UserInfoGateway
│   │       │   └── config/
│   │       │       └── RestConsumerConfig.java
│   │       └── build.gradle
│   │
│   └── entry-points/                      # Adaptadores de ENTRADA
│       └── mcp-server/                    # Servidor MCP (Spring AI)
│           ├── src/main/java/.../mcp/
│           │   ├── tools/                 # 🔧 TOOLS MCP
│           │   │   ├── HealthTool.java    # @McpTool
│           │   │   └── SaludoTool.java    # @McpTool
│           │   │
│           │   ├── resources/             # 📦 RESOURCES MCP
│           │   │   ├── SystemInfoResource.java     # @McpResource
│           │   │   └── UserInfoResource.java       # @McpResource (template)
│           │   │
│           │   └── prompts/               # 💬 PROMPTS MCP
│           │       ├── SaludoPrompt.java      # @McpPrompt
│           │       └── BienvenidaPrompt.java  # @McpPrompt
│           │
│           └── build.gradle
│
└── applications/                          # 🟡 CAPA DE APLICACIÓN
    └── app-service/                       # Ensamblaje y configuración
        ├── src/main/java/.../
        │   ├── MainApplication.java       # Punto de entrada (main)
        │   └── config/
        │       └── UseCasesConfig.java    # Configuración de beans
        │
        ├── src/main/resources/
        │   └── application.yaml           # Configuración de Spring
        │
        └── build.gradle
```

---

## 🎭 Responsabilidades por Capa

### 🔵 Domain (Núcleo del Negocio)

**Ubicación**: `domain/`

**Responsabilidades**:

- Definir entidades de dominio (`UserInfo`)
- Definir interfaces de puertos (`UserInfoGateway`)
- Implementar lógica de negocio pura (`GetUserInfoUseCase`)
- **NO depende de frameworks** (ni Spring, ni Reactor)

**Módulos**:
```
domain/
├── model/       # Entidades y gateways (interfaces)
└── usecase/     # Casos de uso (lógica de negocio)
```

**Ejemplo**:

```java
// domain/model/src/.../UserInfo.java
@Data
@Builder
public class UserInfo {

    private Integer id;
    private String name;
    // ... más campos
}

// domain/model/src/.../gateways/UserInfoGateway.java
public interface UserInfoGateway {

    Mono<UserInfo> getUserInfoById(Integer id);
}

// domain/usecase/src/.../GetUserInfoUseCase.java
public record GetUserInfoUseCase(UserInfoGateway gateway) {

    public Mono<UserInfo> execute(Integer id) {
        return gateway.getUserInfoById(id);
    }
}
```

---

### 🟢 Infrastructure (Detalles de Implementación)

**Ubicación**: `infrastructure/`

**Responsabilidades**:

- **Driven Adapters** (salida): Implementar gateways definidos en el dominio
- **Entry Points** (entrada): Exponer funcionalidad al mundo exterior
- Depende del Domain, pero el Domain NO depende de Infrastructure

#### 🔌 Driven Adapters (Adaptadores de Salida)

**Ubicación**: `infrastructure/driven-adapters/`

**Función**: Implementar los **puertos de salida** (gateways) definidos en el dominio.

**Ejemplo**:

```java
// infrastructure/driven-adapters/rest-consumer/adapters/SimpsonsApiAdapter.java
@Repository
public class SimpsonsApiAdapter implements UserInfoGateway {

    private final RestConsumer client;

    @Override
    public Mono<UserInfo> getUserInfoById(Integer id) {
        return client.getCharacterById(id)
                .map(this::toUserInfo);
    }
}
```

#### 🌐 Entry Points (Adaptadores de Entrada)

**Ubicación**: `infrastructure/entry-points/`

**Función**: Exponer la funcionalidad del sistema al exterior (REST, MCP, GraphQL, etc.).

**MCP Server Structure**:
```
entry-points/mcp-server/
├── tools/       # @McpTool - Funciones ejecutables
├── resources/   # @McpResource - Acceso a datos
└── prompts/     # @McpPrompt - Plantillas de conversación
```

**Ejemplo de Tool**:

```java
// infrastructure/entry-points/mcp-server/tools/SaludoTool.java
@Component
public class SaludoTool {

    @McpTool(name = "saludoTool", description = "...")
    public Mono<String> saludo(
            @McpToolParam(required = true) String name
    ) {
        return Mono.just("¡Hola " + name + "!");
    }
}
```

---

### 🟡 Applications (Ensamblaje)

**Ubicación**: `applications/app-service/`

**Responsabilidades**:

- Arrancar la aplicación (`MainApplication.java`)
- Configurar beans de Spring (`@Configuration`)
- Resolver dependencias
- Configuración global (`application.yaml`)

**Estructura**:

```
app-service/
├── src/main/java/
│   ├── MainApplication.java       # @SpringBootApplication
│   └── config/
│       └── UseCasesConfig.java    # @Configuration para casos de uso
│
└── src/main/resources/
    └── application.yaml            # Configuración de Spring Boot
```

**Ejemplo**:
```java
// applications/app-service/src/.../config/UseCasesConfig.java
@Configuration
public class UseCasesConfig {
    @Bean
    public GetUserInfoUseCase getUserInfoUseCase(UserInfoGateway gateway) {
        return new GetUserInfoUseCase(gateway);
    }
}
```

---

## 🔄 Flujo de Dependencias

### Flujo de Ejecución (Request → Response)

```
1️⃣ Cliente MCP (Claude, Cursor, etc.)
    ↓ HTTP POST /mcp/stream
    
2️⃣ Entry Point: @McpTool / @McpResource / @McpPrompt
    │ (infrastructure/entry-points/mcp-server/)
    ↓ Llama al caso de uso
    
3️⃣ Use Case (domain/usecase/)
    │ Lógica de negocio
    ↓ Usa el gateway (puerto)
    
4️⃣ Gateway Implementation (infrastructure/driven-adapters/)
    │ Llama a API externa, BD, etc.
    ↓ Retorna datos
    
5️⃣ Use Case → Entry Point → Cliente
```

### Diagrama de Dependencias

```
┌─────────────────────────────────────────────────────┐
│  applications/app-service                           │
│  ┌───────────────────────────────────────────────┐  │
│  │  MainApplication + Configurations             │  │
│  └───────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────┘
                     │ ensambla
                     ↓
┌─────────────────────────────────────────────────────┐
│  infrastructure/                                    │
│  ┌──────────────────┐      ┌──────────────────┐    │
│  │  entry-points/   │      │ driven-adapters/ │    │
│  │  mcp-server      │ ───→ │ rest-consumer    │    │
│  └──────────────────┘      └──────────────────┘    │
└────────────────────┬────────────────┬───────────────┘
                     │                │
                     └────────┬───────┘
                              │ implementa
                              ↓
                   ┌─────────────────────┐
                   │  domain/            │
                   │  ┌────────────────┐ │
                   │  │ model/         │ │
                   │  │  - UserInfo    │ │
                   │  │  - Gateways    │ │
                   │  └────────────────┘ │
                   │  ┌────────────────┐ │
                   │  │ usecase/       │ │
                   │  │  - UseCases    │ │
                   │  └────────────────┘ │
                   └─────────────────────┘
```

---

## 📦 Ubicación de Archivos por Tipo

| Tipo de Clase                    | Módulo                     | Paquete                                      | Anotación                     |
|----------------------------------|----------------------------|----------------------------------------------|-------------------------------|
| Entidad de Dominio               | `model`                    | `co.com.bancolombia.model.{entity}`          | `@Data`, `@Builder`           |
| Gateway (Interface)              | `model`                    | `co.com.bancolombia.model.{entity}.gateways` | (interface)                   |
| Use Case                         | `usecase`                  | `co.com.bancolombia.usecase`                 | `record` o clase              |
| Adapter (Implementación Gateway) | `driven-adapter/{adapter}` | `co.com.bancolombia.{adapter}.adapters`      | `@Repository` o `@Component`  |
| MCP Tool                         | `mcp-server`               | `co.com.bancolombia.mcp.tools`               | `@Component` + `@McpTool`     |
| MCP Resource                     | `mcp-server`               | `co.com.bancolombia.mcp.resources`           | `@Component` + `@McpResource` |
| MCP Prompt                       | `mcp-server`               | `co.com.bancolombia.mcp.prompts`             | `@Component` + `@McpPrompt`   |
| Configuración                    | `app-service`              | `co.com.bancolombia.config`                  | `@Configuration`              |
| Main Application                 | `app-service`              | `co.com.bancolombia`                         | `@SpringBootApplication`      |

---

## ⚙️ Convenciones

### ✅ DO (Hacer)

- ✅ **Domain NO depende de nadie** (ni Spring, ni frameworks)
- ✅ **Use Cases como records** cuando no tienen estado
- ✅ **Interfaces (Gateways) en model/gateways/**
- ✅ **Implementaciones en infrastructure/**
- ✅ **@Configuration solo en applications/app-service**
- ✅ **Métodos reactivos (`Mono<T>`)** para servidores ASYNC
- ✅ **Logs estructurados** con SLF4J

### ❌ DON'T (No hacer)

- ❌ **NO poner @Configuration en infrastructure**
- ❌ **NO poner lógica de negocio en entry-points**
- ❌ **NO hacer que domain dependa de infrastructure**
- ❌ **NO usar tipos síncronos** en servidores ASYNC MCP
- ❌ **NO mezclar responsabilidades de capas**

---

## 🔍 Ejemplos Prácticos

### Agregar un nuevo Tool

1. **Crear la clase en** `infrastructure/entry-points/mcp-server/tools/`:

```java

@Component
public class MiNuevoTool {

    @McpTool(
            name = "miNuevoTool",
            description = "Hace algo útil"
    )
    public Mono<String> ejecutar(
            @McpToolParam(required = true) String parametro
    ) {
        return Mono.just("Resultado: " + parametro);
    }
}
```

2. **Spring AI lo detecta automáticamente** (no necesitas configuración adicional)

### Agregar un nuevo Use Case

1. **Crear el caso de uso en** `domain/usecase/`:
```java
public record MiNuevoUseCase(MiGateway gateway) {

    public Mono<MiEntidad> execute(Integer id) {
        return gateway.obtener(id);
    }
}
```

2. **Crear el bean en** `applications/app-service/config/UseCasesConfig.java`:
```java

@Bean
public MiNuevoUseCase miNuevoUseCase(MiGateway gateway) {
    return new MiNuevoUseCase(gateway);
}
```

---

## 🚀 Comandos Útiles

```bash
# Compilar el proyecto
./gradlew clean build

# Ejecutar tests
./gradlew test

# Ejecutar la aplicación
./gradlew bootRun

# Ver dependencias
./gradlew dependencies

# Generar reporte de cobertura
./gradlew jacocoTestReport
```

---

## 📚 Referencias

- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Clean Architecture - Bancolombia](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)
- [Scaffold Generator - Bancolombia](https://github.com/bancolombia/scaffold-clean-architecture)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI MCP](https://docs.spring.io/spring-ai/reference/api/mcp/)
- [Project Reactor](https://projectreactor.io/docs)