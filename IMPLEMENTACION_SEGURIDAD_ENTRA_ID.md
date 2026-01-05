# Implementación de Seguridad con Microsoft Entra ID (Azure AD)

Este documento detalla la arquitectura de seguridad final implementada para el servidor MCP,
reemplazando las propuestas anteriores. La solución se basa en el estándar **OAuth2 / OIDC**
utilizando **Microsoft Entra ID** como proveedor de identidad.

## 1. Autenticación y Validación de Tokens (JWT)

Se ha configurado `McpSecurityConfig` para manejar tokens JWT de manera robusta, soportando tanto
usuarios como aplicaciones (Service Principals/Managed Identities).

### Lógica de Validación Implementada

Para solucionar problemas comunes con tokens de Azure AD (v1 vs v2, prefijos `api://`, tokens de
Graph), se implementó una validación personalizada:

* **Validador de Timestamp**: Se verifica `exp` (expiración) y `nbf` (not before).
* **Validación de Audience (`aud`) Flexible**:
    * Se acepta el token si el claim `aud` **contiene** el `Client ID` de la aplicación.
    * Esto maneja automáticamente el prefijo `api://` que Azure añade a veces.
* **Fallback a App ID**:
    * Si la validación de `aud` falla (por ejemplo, tokens emitidos para *Microsoft Graph*), se
      verifica el claim `appid`.
    * Si `appid` coincide con el `Client ID`, el token se acepta. Esto permite que la propia
      aplicación se llame a sí misma o use tokens on-behalf-of de manera más flexible.
* **Issuer Relajado**: No se fuerza una coincidencia exacta de la URL del issuer (útil para soportar
  `sts.windows.net` vs `login.microsoftonline.com` sin configuración extra).

**Archivo**: `McpSecurityConfig.java`

## 2. Configuración CORS Centralizada

Se consolidó la configuración de CORS para eliminar problemas de conectividad con clientes web y
herramientas de desarrollo.

* **Perfil `default`**: Si no se activa ningún perfil específico, se carga una configuración *
  *permisiva** (`*`).
    * Permite todos los orígenes, métodos y headers.
    * Compatible con **MCP Inspector**, **LangChain**, aplicaciones locales y cURL.
* **Perfiles `dev`, `qa`, `pdn`**: Mantienen la capacidad de restringir orígenes permitidos vía
  configuración (`cors.allowed-origin`).

**Archivo**: `CorsConfig.java`

## 3. Auditoría e Identidad Reactiva (AOP)

El sistema de auditoría (`ApiKeyAuditAspect`) fue refactorizado para funcionar correctamente en el
entorno **WebFlux** (no bloqueante).

### Cambios Clave

* **Contexto Reactivo**: Se usa `ReactiveSecurityContextHolder` en lugar de
  `SecurityContextHolder` (ThreadLocal), asegurando que se capture la identidad en flujos
  asíncronos.
* **Extracción de Identidad Avanzada**: En lugar de mostrar "anonymous", el aspecto ahora extrae la
  identidad real del JWT buscando en orden:
    1. `appid`: Identidad de aplicación (Azure AD).
    2. `azp`: Authorized Party (estándar OIDC).
    3. `aud`: Audiencia (si no hay otro identificador).
    4. `sub`: Subject (usuario).

**Archivo**: `ApiKeyAuditAspect.java`

## 4. Dependencias

Para soportar las capacidades de extracción de claims y tipos JWT en el módulo `mcp-server` (donde
reside el aspecto de auditoría), se agregó la dependencia:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

## Resumen de Flujo

1. **Cliente** obtiene token de Entra ID.
2. **Cliente** llama al MCP Server con `Authorization: Bearer <token>`.
3. **CorsWebFilter** permite la petición (configuración centralizada).
4. **McpSecurityConfig** valida firma y reglas de negocio (`aud` o `appid`).
5. **ApiKeyAuditAspect** intercepta la llamada, extrae el `appid` del token de forma reactiva y
   registra la ejecución.
6. **Tool/Resource** se ejecuta.
