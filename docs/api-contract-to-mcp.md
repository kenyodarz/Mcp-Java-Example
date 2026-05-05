# API Contract → MCP Traceability Matrix

> Generado según el workflow `agent-api-to-mcp-workflow.md` (Fase 5 – Cleanup & Approval)
>
> Proyecto: `mcp-bancolombia` | Versión: `1.0.0`

---

## 📋 Regla de Trazabilidad 1:1

> **1 HTTP Method = 1 MCP Primitive + 1 UseCase + 1 Adapter**

---

## 🔧 MCP Tools (Acciones con efectos secundarios)

| # | HTTP Method / Operación | MCP Tool                          | UseCase                  | Adapter / Gateway                        | Rol de Seguridad                    | Estado |
|---|-------------------------|-----------------------------------|--------------------------|------------------------------------------|-------------------------------------|--------|
| 1 | `GET /characters/{id}`  | `get_character` (`SimpsonsTools`) | `GetCharacterUseCase`    | `SimpsonsApiAdapter` → `SimpsonsGateway` | `MCP.TOOL.SIMPSONS`, `MCP.ADMIN`    | ✅ OK   |
| 2 | `GET /episodes/{id}`    | `get_episode` (`SimpsonsTools`)   | `GetEpisodeUseCase`      | `SimpsonsApiAdapter` → `SimpsonsGateway` | `MCP.TOOL.SIMPSONS`, `MCP.ADMIN`    | ✅ OK   |
| 3 | `GET /locations/{id}`   | `get_location` (`SimpsonsTools`)  | `GetLocationUseCase`     | `SimpsonsApiAdapter` → `SimpsonsGateway` | `MCP.TOOL.SIMPSONS`, `MCP.ADMIN`    | ✅ OK   |
| 4 | N/A (Interacción)       | `saludoTool` (`SaludoTool`)       | `SaludoUseCase`          | N/A (dominio puro)                       | `MCP.TOOL.INTERACTION`, `MCP.ADMIN` | ✅ OK   |
| 5 | N/A (Health)            | `healthCheck` (`HealthTool`)      | N/A (respuesta estática) | N/A                                      | `MCP.TOOL.HEALTH`, `MCP.ADMIN`      | ✅ OK   |

---

## 📦 MCP Resources (Consultas de estado – Solo lectura)

| # | URI Resource                | MCP Resource                               | UseCase              | Adapter / Gateway                        | Rol de Seguridad                        | Estado               |
|---|-----------------------------|--------------------------------------------|----------------------|------------------------------------------|-----------------------------------------|----------------------|
| 1 | `resource://users/{userId}` | `user-info` (`UserInfoResource`)           | `GetUserInfoUseCase` | `SimpsonsApiAdapter` → `UserInfoGateway` | `MCP.RESOURCE.USER.READ`, `MCP.ADMIN`   | ✅ OK                 |
| 2 | `resource://system/info`    | `system-info` (`SystemInfoResource`)       | N/A (info estática)  | N/A                                      | `MCP.RESOURCE.SYSTEM.READ`, `MCP.ADMIN` | ✅ OK                 |
| 3 | `simpsons://character/{id}` | `simpsons-character` (`SimpsonsResources`) | — ⚠️ Ver nota        | `SimpsonsApiAdapter` → `SimpsonsGateway` | `MCP.RESOURCE.SIMPSONS`, `MCP.ADMIN`    | ⚠️ Pendiente UseCase |
| 4 | `simpsons://episode/{id}`   | `simpsons-episode` (`SimpsonsResources`)   | — ⚠️ Ver nota        | `SimpsonsApiAdapter` → `SimpsonsGateway` | `MCP.RESOURCE.SIMPSONS`, `MCP.ADMIN`    | ⚠️ Pendiente UseCase |
| 5 | `simpsons://location/{id}`  | `simpsons-location` (`SimpsonsResources`)  | — ⚠️ Ver nota        | `SimpsonsApiAdapter` → `SimpsonsGateway` | `MCP.RESOURCE.SIMPSONS`, `MCP.ADMIN`    | ⚠️ Pendiente UseCase |

> **Nota**: `SimpsonsResources` actualmente llama directamente al `SimpsonsGateway`. Los Resources
> de Simpsons pueden reutilizar los UseCases `GetCharacterUseCase`, `GetEpisodeUseCase`,
`GetLocationUseCase` como mejora evolutiva.

---

## 💬 MCP Prompts (Conversación estructurada)

| # | Prompt                                 | UseCase                        | Rol de Seguridad                   | Estado |
|---|----------------------------------------|--------------------------------|------------------------------------|--------|
| 1 | `bienvenida` (`BienvenidaPrompt`)      | N/A (plantilla conversacional) | `MCP.PROMPT.BASIC`, `MCP.ADMIN`    | ✅ OK   |
| 2 | `saludo` (`SaludoPrompt`)              | N/A (plantilla conversacional) | `MCP.PROMPT.BASIC`, `MCP.ADMIN`    | ✅ OK   |
| 3 | `perfil_personaje` (`SimpsonsPrompts`) | N/A (plantilla conversacional) | `MCP.PROMPT.SIMPSONS`, `MCP.ADMIN` | ✅ OK   |
| 4 | `resumen_episodio` (`SimpsonsPrompts`) | N/A (plantilla conversacional) | `MCP.PROMPT.SIMPSONS`, `MCP.ADMIN` | ✅ OK   |

---

## 🏗️ Cadenas Arquitecturales Validadas

```
MCP Tool (entry-point)
  └─→ UseCase (domain/usecase)
        └─→ Gateway Interface (domain/model/gateways)
              └─→ Driven Adapter (infrastructure/driven-adapters)
                    └─→ RestConsumer (WebClient)
                          └─→ Simpsons External API
```

---

## 🔒 Validación de Seguridad (Fail-Closed)

| Componente                      | Protección                      | Estado |
|---------------------------------|---------------------------------|--------|
| `McpSecurityConfig`             | OAuth2 + JWT (Entra ID)         | ✅ OK   |
| `@EnableReactiveMethodSecurity` | Activo en configuración         | ✅ OK   |
| `anyExchange().authenticated()` | FAIL-CLOSED activo              | ✅ OK   |
| Todos los `@McpTool`            | `@PreAuthorize` presente        | ✅ OK   |
| Todos los `@McpResource`        | `@PreAuthorize` presente        | ✅ OK   |
| Todos los `@McpPrompt`          | `@PreAuthorize` presente        | ✅ OK   |
| `ApiKeyAuditAspect`             | Auditoría de todas las llamadas | ✅ OK   |

---

## ⚙️ Configuración Sin Hardcoding

| Propiedad                                              | Variable de Entorno               | Estado |
|--------------------------------------------------------|-----------------------------------|--------|
| `adapter.restconsumer.url`                             | `${SIMPSONS_API_URL:https://...}` | ✅ OK   |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `${AZURE_ISSUER_URI:...}`         | ✅ OK   |
| `spring.security.oauth2.resourceserver.jwt.client-id`  | `${AZURE_CLIENT_ID:...}`          | ✅ OK   |

---

## ✅ Definition of Done (Fase 5)

- [x] Clases Sample/Placeholder eliminadas de la ruta crítica
- [x] Matriz de trazabilidad completa (`docs/api-contract-to-mcp.md`)
- [x] Build sin errores: `./gradlew build` → **BUILD SUCCESSFUL**
- [x] Tests sin fallos: `./gradlew test`
- [x] Seguridad Fail-Closed: `anyExchange().authenticated()`
- [x] Cero hardcoding: todas las URLs en `application.yaml` con `${ENV_VAR}`
- [x] Paquete MCP correcto: `org.springframework.ai.mcp.annotation` (Spring AI 2.0.0-M5)
- [x] Dependencia AOP correcta: `spring-boot-starter-aspectj` (Spring Boot 4.x BOM)
- [x] Granularidad 1:1: cada Tool delega a su propio UseCase
- [x] Módulo `r2dbc-postgresql` removido del ensamblado por no estar en uso

