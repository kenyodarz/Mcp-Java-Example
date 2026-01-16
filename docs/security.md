# Security

Guía completa de seguridad del Code Review MCP Server, incluyendo autenticación, gestión de API
Keys, y auditoría.

---

## 🔐 Descripción General

El servidor utiliza **autenticación basada en API Keys** almacenadas en una base de datos H2 con
hashing BCrypt.

### Características de Seguridad

✅ **API Keys en H2**: Base de datos en memoria para desarrollo (migrable a PostgreSQL/MySQL en
producción)  
✅ **Hashing con BCrypt**: Secrets nunca se almacenan en texto plano  
✅ **Rotación Automática**: API Keys expiradas se desactivan automáticamente  
✅ **Auditoría Completa**: Logs detallados de todos los accesos  
✅ **Expiración Configurable**: API Keys con fecha de vencimiento  
✅ **Control por IP** (opcional): Restricción de IPs permitidas

---

## 🔑 API Keys Predefinidas

Al iniciar la aplicación, se crean automáticamente estas API Keys:

### 1. dev-client (Desarrollo)

```
ID: dev-client
Secret: dev-secret-key-12345
Full Key: dev-client.dev-secret-key-12345
Expira: 1 año desde creación
Descripción: Para desarrollo local
```

### 2. qa-client (QA/Testing)

```
ID: qa-client
Secret: qa-secret-key-67890
Full Key: qa-client.qa-secret-key-67890
Expira: 6 meses desde creación
Descripción: Para ambiente de QA
```

### 3. claude-desktop (Claude Desktop)

```
ID: claude-desktop
Secret: claude-secret-key-abcde
Full Key: claude-desktop.claude-secret-key-abcde
Expira: Nunca
Descripción: Para Claude Desktop App
```

### 4. disabled-client (Deshabilitada)

```
ID: disabled-client
Estado: DESHABILITADA
Uso: Para testing de clientes deshabilitados
```

---

## 📡 Uso de API Keys

### Formato

Las API Keys tienen el formato: **`{id}.{secret}`**

Ejemplo: `dev-client.dev-secret-key-12345`

### Header HTTP

Todas las requests deben incluir el header:

```
X-API-Key: {id}.{secret}
```

### Ejemplo con cURL

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/list"
  }'
```

### Configuración en Claude Desktop

Edita el archivo de configuración:

- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "bancolombia-code-review": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-sse-client",
        "http://localhost:8080/mcp/stream"
      ],
      "env": {
        "X-API-Key": "claude-desktop.claude-secret-key-abcde"
      }
    }
  }
}
```

---

## 🛠️ Gestión de API Keys

### Ver API Keys en H2 Console

1. Acceder a: `http://localhost:8080/h2-console`
2. Credenciales:
    - **JDBC URL**: `jdbc:h2:mem:mcpdb`
    - **User**: `sa`
    - **Password**: (dejar vacío)

3. Ejecutar query:

```sql
SELECT id, name, enabled, created_at, expires_at, usage_count, last_used_at
FROM api_keys;
```

### Crear Nueva API Key

```sql
-- 1. Generar hash BCrypt del secret
-- Usar: https://bcrypt-generator.com/ (rounds: 10)

INSERT INTO api_keys (id,
                      name,
                      secret_hash,
                      enabled,
                      description,
                      created_at,
                      updated_at,
                      expires_at)
VALUES ('nuevo-cliente',
        'Mi Nuevo Cliente',
        '$2a$10$HASH_GENERADO_AQUI',
        TRUE,
        'Descripción del nuevo cliente',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        DATEADD('MONTH', 6, CURRENT_TIMESTAMP));
```

### Desactivar API Key

```sql
UPDATE api_keys
SET enabled    = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'dev-client';
```

### Extender Expiración

```sql
UPDATE api_keys
SET expires_at = DATEADD('MONTH', 12, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'dev-client';
```

### Ver Estadísticas de Uso

```sql
SELECT id,
       name,
       usage_count,
       last_used_at,
       CASE
           WHEN last_used_at IS NULL THEN 'Nunca usada'
           WHEN DATEDIFF('DAY', last_used_at, CURRENT_TIMESTAMP) = 0 THEN 'Hoy'
           WHEN DATEDIFF('DAY', last_used_at, CURRENT_TIMESTAMP) = 1 THEN 'Ayer'
           ELSE CONCAT(DATEDIFF('DAY', last_used_at, CURRENT_TIMESTAMP), ' días')
           END as ultimo_uso
FROM api_keys
ORDER BY usage_count DESC;
```

---

## 🔄 Rotación Automática

### Configuración

La rotación se configura en `application.yaml`:

```yaml
security:
  apikey:
    rotation:
      enabled: true
      cron: "0 0 2 * * *"  # Cada día a las 2:00 AM
    notification:
      cron: "0 0 9 * * MON"  # Cada lunes a las 9:00 AM
      days-before-expiration: 30
```

### Tareas Programadas

#### 1. Rotación de Keys Expiradas

**Frecuencia**: Diaria a las 2:00 AM  
**Acción**: Desactiva automáticamente API Keys expiradas

```
2025-11-24 02:00:00 - 🔄 Iniciando tarea programada: Rotación de API Keys expiradas
2025-11-24 02:00:01 - ⚠️ Desactivando API Key expirada: qa-client (expiró el: 2025-11-24T00:00:00)
2025-11-24 02:00:02 - ✅ API Keys rotadas: 1
```

#### 2. Notificación de Keys por Expirar

**Frecuencia**: Lunes a las 9:00 AM  
**Acción**: Reporta API Keys que expiran en los próximos 30 días

```
2025-11-25 09:00:00 - 📧 Iniciando tarea programada: Notificación de API Keys próximas a expirar
2025-11-25 09:00:01 - ⚠️ API Keys próximas a expirar: 2
2025-11-25 09:00:01 -    - dev-client: Cliente de Desarrollo (expira: 2025-12-20T10:00:00)
2025-11-25 09:00:01 -    - qa-client: Cliente de QA (expira: 2025-12-15T14:30:00)
```

### Deshabilitar Rotación

```yaml
security:
  apikey:
    rotation:
      enabled: false
```

---

## 📊 Auditoría

Todos los accesos al servidor MCP se auditan automáticamente.

### Formato de Logs

```
[TIMESTAMP] [NIVEL] [TIPO] API Key: {id} | Método: {clase}.{método} | Args: [{args}] | Tiempo: {ms}ms
```

### Ejemplos de Logs

#### Tool Call Exitoso

```
2025-11-23 14:30:45 - 📊 [AUDIT] TOOL llamado por API Key: dev-client | Método: HealthTool.health | Args: []
2025-11-23 14:30:45 - ✅ [AUDIT] TOOL exitoso | API Key: dev-client | Método: HealthTool.health | Tiempo: 25ms
```

#### Resource Access

```
2025-11-23 14:31:12 - 📊 [AUDIT] RESOURCE llamado por API Key: claude-desktop | Método: UserInfoResource.getUserInfo | Args: [1]
2025-11-23 14:31:15 - ✅ [AUDIT] RESOURCE exitoso | API Key: claude-desktop | Método: UserInfoResource.getUserInfo | Tiempo: 3200ms
```

#### Acceso Denegado

```
2025-11-23 14:32:00 - ⚠️ API Key no encontrada: invalid-client
2025-11-23 14:32:00 - ❌ [AUDIT] TOOL fallido | API Key: anonymous | Método: HealthTool.health | Tiempo: 5ms | Error: Unauthorized
```

### Buscar Logs por API Key

```bash
# En el log file
grep "API Key: dev-client" logs/application.log

# En consola (durante ejecución)
tail -f logs/application.log | grep "dev-client"
```

### Métricas de Uso

```sql
-- Top 5 API Keys más usadas
SELECT id, name, usage_count, last_used_at
FROM api_keys
WHERE enabled = TRUE
ORDER BY usage_count DESC
LIMIT 5;

-- API Keys no usadas en los últimos 30 días
SELECT id, name, last_used_at
FROM api_keys
WHERE enabled = TRUE
  AND (last_used_at IS NULL OR last_used_at < DATEADD('DAY', -30, CURRENT_TIMESTAMP));
```

---

## 🔒 Mejores Prácticas

### Para Desarrollo

✅ Usar API Keys de desarrollo con expiración corta  
✅ Rotar secrets regularmente  
✅ No commitear secrets en Git  
✅ Usar variables de entorno en CI/CD  
✅ Habilitar H2 Console solo en desarrollo

### Para Producción

✅ **Cambiar a base de datos persistente** (PostgreSQL, MySQL)  
✅ **Implementar HTTPS obligatorio**  
✅ **Restringir IPs** en campo `allowed_ip`  
✅ **Configurar expiración automática**  
✅ **Monitorear uso de API Keys**  
✅ **Implementar rate limiting**  
✅ **Deshabilitar H2 Console**  
✅ **Migrar a OAuth2** para casos enterprise

### Configuración Recomendada para Producción

```yaml
spring:
  h2:
    console:
      enabled: false  # Deshabilitar H2 Console

  datasource:
    url: jdbc:postgresql://localhost:5432/mcpdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

security:
  apikey:
    rotation:
      enabled: true
      cron: "0 0 2 * * *"
    notification:
      cron: "0 0 9 * * MON"
      days-before-expiration: 30

server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
```

---

## 🐛 Troubleshooting de Seguridad

### Error: "401 Unauthorized"

**Causa**: API Key inválida, inexistente o deshabilitada

**Solución**:

1. Verificar formato: `id.secret`
2. Verificar que exista en H2:

```sql
SELECT *
FROM api_keys
WHERE id = 'tu-id';
```

3. Verificar que esté activa: `enabled = TRUE`
4. Verificar que no haya expirado: `expires_at > NOW()`

### Error: "Header X-API-Key not found"

**Causa**: No se envió el header de autenticación

**Solución**: Agregar header en la request:

```
X-API-Key: dev-client.dev-secret-key-12345
```

### API Key expira inmediatamente

**Causa**: Zona horaria incorrecta o fecha de expiración pasada

**Solución**:

```sql
UPDATE api_keys
SET expires_at = DATEADD('YEAR', 1, CURRENT_TIMESTAMP)
WHERE id = 'tu-id';
```

### Rotación no funciona

**Verificar**:

1. Que esté habilitada en `application.yaml`:

```yaml
security.apikey.rotation.enabled: true
```

2. Revisar logs del scheduler:

```bash
grep "tarea programada" logs/application.log
```

---

**💡 Tip**: Para desarrollo local, usa la API Key `dev-client.dev-secret-key-12345`. Para producción,
crea API Keys específicas con expiración configurada y migra a una base de datos persistente.

Para más información:

- [Getting Started](getting-started.md)
- [API Reference](api-reference.md)
- [Troubleshooting](troubleshooting.md)
