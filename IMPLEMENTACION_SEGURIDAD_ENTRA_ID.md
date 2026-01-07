# Implementación de Seguridad con Microsoft Entra ID (Azure AD)

Este documento detalla la arquitectura de seguridad final implementada para el servidor MCP,
reemplazando las propuestas anteriores. La solución se basa en el estándar **OAuth2 / OIDC**
utilizando **Microsoft Entra ID** como proveedor de identidad.

## 🔑 Conceptos Clave: Scopes vs Roles

Antes de profundizar en la implementación técnica, es crucial entender la diferencia conceptual:

| Criterio             | **Scopes** (`scp`)                      | **Roles** (`roles`)                      |
|:---------------------|:----------------------------------------|:-----------------------------------------|
| **Relación**         | Uno a uno (Consumidor específico ↔ API) | Muchos a uno (Muchos consumidores ↔ API) |
| **Granularidad**     | Muy fina (Acción específica)            | Media / Gruesa (Capacidad o Perfil)      |
| **Consentimiento**   | Relevante (Usuario aprueba acceso)      | No aplica (Asignación administrativa)    |
| **Pre-autorización** | Sí                                      | No                                       |
| **Ideal para**       | Acciones puntuales (`users.read`)       | Capacidades del sistema (`MCP.ADMIN`)    |
| **Flujo M2M**        | Solo para recursos muy sensibles        | **Estándar para interacciones M2M**      |

> **Regla de Oro**:
> * **Scopes** = "Puede hacer esta acción específica"
> * **Roles** = "Este sistema tiene este tipo de acceso"
>
> En flujos Machine-to-Machine (M2M), los App Roles se incluyen en el token únicamente cuando están
> asignados al Service Principal consumidor.

---

## 1. Control de Acceso Basado en Roles (RBAC) Implemented

Se ha implementado una estrategia de seguridad basada en **Roles (`roles` claim)** para proteger los
componentes del servidor MCP.

### Tabla de Auditoría de Roles

| Componente MCP             | Método               | Rol Requerido (Token)                    |
|:---------------------------|:---------------------|:-----------------------------------------|
| **Prompt** `bienvenida`    | `BienvenidaPrompt`   | `MCP.PROMPT.BASIC` o `MCP.ADMIN`         |
| **Prompt** `saludo`        | `SaludoPrompt`       | `MCP.PROMPT.BASIC` o `MCP.ADMIN`         |
| **Tool** `healthCheck`     | `HealthTool`         | `MCP.TOOL.HEALTH` o `MCP.ADMIN`          |
| **Tool** `saludoTool`      | `SaludoTool`         | `MCP.TOOL.INTERACTION` o `MCP.ADMIN`     |
| **Resource** `system/info` | `SystemInfoResource` | `MCP.RESOURCE.SYSTEM.READ` o `MCP.ADMIN` |
| **Resource** `users/{id}`  | `UserInfoResource`   | `MCP.RESOURCE.USER.READ` o `MCP.ADMIN`   |

> **Nota**: El rol `MCP.ADMIN` tiene acceso universal de "bypass" en todos los componentes.

### Implementación Técnica

* **Habilitación**: `@EnableReactiveMethodSecurity` en `McpSecurityConfig`.
* **Anotación**: Uso de `@PreAuthorize("hasAnyRole('ROL_ESPECIFICO', 'MCP.ADMIN')")` directamente en
  los métodos de las clases de Tools/Resources.
* **Manejo de Errores**: Se implementó un `AccessDeniedHandler` personalizado en `McpSecurityConfig`
  para loguear explícitamente los rechazos de seguridad (403 Forbidden) que de otro modo serían
  silenciosos.

## 2. Autenticación y Validación de Tokens (JWT)

Se ha configurado `McpSecurityConfig` para manejar tokens JWT de manera robusta.

### Lógica de Validación

* **Validador de Timestamp**: Verifica `exp` y `nbf`.
* **Audience (`aud`) Flexible**: Acepta el token si `aud` contiene el `Client ID` (soporta prefijo
  `api://`).
* **Fallback a App ID**: Si `aud` falla (ej. tokens de Graph), verifica el claim `appid`.
* **Issuer Relajado**: Soporta variaciones de issuer de Azure AD.

## 3. Configuración CORS Centralizada

Configuración en `CorsConfig.java`:

* **Perfil `default`**: Permisivo (`*`) para facilitar desarrollo y uso con MCP Inspector/Agentes.
* **Perfiles `dev/qa`**: Restrictivos según configuración.

## 4. Auditoría e Identidad Reactiva (AOP)

Aspecto `ApiKeyAuditAspect`:

* **Contexto Reactivo**: Usa `ReactiveSecurityContextHolder` para extraer identidad en WebFlux.
* **Orden de Ejecución**: Anotado con `@Order(Ordered.HIGHEST_PRECEDENCE)` para asegurar que se
  ejecute **antes** y **después** de la seguridad de Spring. Esto permite auditar intentos fallidos
  de acceso (403) que antes eran invisibles.
* **Identidad**: Extrae `appid` > `azp` > `aud` > `sub`.

## 5. Dependencias Clave

* `spring-boot-starter-oauth2-resource-server`
* `mcp-server-security`
