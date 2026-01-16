# API Reference

Documentación completa de las herramientas, recursos y prompts disponibles en el Code Review MCP
Server.

---

## 🔧 Tools (Herramientas)

Los Tools son funciones ejecutables que los clientes MCP pueden invocar.

### `analyze_repository`

Inicia el análisis estático de un repositorio de código desde Azure DevOps.

**Descripción**: Analiza un repositorio Java con Clean Architecture y genera un reporte con
hallazgos y recomendaciones.

**Parámetros**:

| Parámetro         | Tipo   | Requerido | Descripción                                                |
|-------------------|--------|-----------|------------------------------------------------------------|
| `repository_name` | String | Sí        | Nombre exacto del repositorio en Azure DevOps              |
| `path`            | String | No        | Ruta relativa dentro del repositorio (útil para monorepos) |
| `branch`          | String | No        | Rama a analizar (default: `trunk`, `main` o `master`)      |

**Retorno**:

```json
{
  "execution_id": "arn:aws:states:us-east-1:123456789012:execution:CodeReviewStateMachine:abc123"
}
```

**Ejemplo con cURL**:

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/call",
    "params": {
      "name": "analyze_repository",
      "arguments": {
        "repository_name": "My-Java-Project",
        "branch": "develop",
        "path": "/code_review_smcp"
      }
    }
  }'
```

**Ejemplo con Claude**:

```
Por favor analiza el repositorio "My-Java-Project" en la rama "develop"
```

---

### `check_status`

Consulta el estado actual de un análisis en ejecución.

**Descripción**: Verifica si el análisis sigue en curso, falló o se completó exitosamente.

**Parámetros**:

| Parámetro      | Tipo   | Requerido | Descripción                                           |
|----------------|--------|-----------|-------------------------------------------------------|
| `execution_id` | String | Sí        | El ID de ejecución retornado por `analyze_repository` |

**Retorno**:

```json
{
  "status": "RUNNING"
}
```

**Estados posibles**:

- `RUNNING`: Análisis en progreso
- `SUCCEEDED`: Análisis completado exitosamente
- `FAILED`: Análisis falló

**Ejemplo con cURL**:

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/call",
    "params": {
      "name": "check_status",
      "arguments": {
        "execution_id": "arn:aws:states:us-east-1:123456789012:execution:CodeReviewStateMachine:abc123"
      }
    }
  }'
```

---

### `get_results`

Obtiene el reporte final de un análisis completado exitosamente.

**Descripción**: Descarga y presenta los resultados del análisis desde S3.

**Parámetros**:

| Parámetro      | Tipo   | Requerido | Descripción                                |
|----------------|--------|-----------|--------------------------------------------|
| `execution_id` | String | Sí        | El ID de ejecución del análisis completado |

**Retorno**:

```json
{
  "content": "Análisis completado. Hallazgos: ...",
  "file": "s3://code-review-results/abc123/analysis-report.json"
}
```

**Ejemplo con cURL**:

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/call",
    "params": {
      "name": "get_results",
      "arguments": {
        "execution_id": "arn:aws:states:us-east-1:123456789012:execution:CodeReviewStateMachine:abc123"
      }
    }
  }'
```

---

### `health`

Verifica el estado del servidor MCP.

**Descripción**: Health check simple para verificar que el servidor está funcionando.

**Parámetros**: Ninguno

**Retorno**:

```json
{
  "status": "healthy",
  "timestamp": "2025-11-24T10:30:00Z"
}
```

**Ejemplo con cURL**:

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/call",
    "params": {
      "name": "health",
      "arguments": {}
    }
  }'
```

---

## 📄 Resources (Recursos)

Los Resources proporcionan acceso a datos del sistema.

### `system-info`

Información del sistema donde se ejecuta el servidor.

**URI**: `resource://system/info`

**Descripción**: Retorna información sobre el sistema operativo, JVM, y recursos disponibles.

**Retorno**:

```json
{
  "contents": [
    {
      "uri": "resource://system/info",
      "mimeType": "application/json",
      "text": "{\"os\":\"Linux\",\"jvm\":\"17.0.8\",\"memory\":\"2GB\"}"
    }
  ]
}
```

**Ejemplo con cURL**:

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "resources/read",
    "params": {
      "uri": "resource://system/info"
    }
  }'
```

---

### `user-info` (Template)

Información de un usuario específico.

**URI**: `resource://users/{userId}`

**Descripción**: Retorna información detallada de un usuario dado su ID.

**Parámetros**:

- `userId`: ID del usuario (en la URI)

**Retorno**:

```json
{
  "contents": [
    {
      "uri": "resource://users/1",
      "mimeType": "application/json",
      "text": "{\"id\":1,\"name\":\"Homer Simpson\",\"email\":\"homer@springfield.com\"}"
    }
  ]
}
```

**Ejemplo con cURL**:

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "resources/read",
    "params": {
      "uri": "resource://users/1"
    }
  }'
```

---

## 💬 Prompts (Plantillas)

Los Prompts son plantillas de conversación predefinidas.

### `greeting`

Prompt de saludo personalizable.

**Descripción**: Genera un mensaje de bienvenida personalizado.

**Argumentos**:

| Argumento | Tipo   | Requerido | Descripción                    |
|-----------|--------|-----------|--------------------------------|
| `name`    | String | Sí        | Nombre de la persona a saludar |

**Retorno**:

```json
{
  "description": "Saludo personalizado",
  "messages": [
    {
      "role": "user",
      "content": {
        "type": "text",
        "text": "Hola Jorge, ¿en qué te ayudo?"
      }
    }
  ]
}
```

**Ejemplo con cURL**:

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "prompts/get",
    "params": {
      "name": "greeting",
      "arguments": {
        "name": "Jorge"
      }
    }
  }'
```

---

## 📋 Listar Capacidades

### Listar Tools Disponibles

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/list"
  }'
```

**Respuesta**:

```json
{
  "tools": [
    {
      "name": "analyze_repository",
      "description": "Inicia el análisis estático de un repositorio",
      "inputSchema": {
        ...
      }
    },
    {
      "name": "check_status",
      "description": "Consulta el estado de un análisis",
      "inputSchema": {
        ...
      }
    },
    {
      "name": "get_results",
      "description": "Obtiene los resultados de un análisis",
      "inputSchema": {
        ...
      }
    },
    {
      "name": "health",
      "description": "Verifica el estado del servidor",
      "inputSchema": {
        ...
      }
    }
  ]
}
```

### Listar Resources Disponibles

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "resources/list"
  }'
```

### Listar Prompts Disponibles

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "prompts/list"
  }'
```

---

## 🔄 Flujo Completo de Análisis

### 1. Iniciar Análisis

```json
POST /mcp/stream
{
  "method": "tools/call",
  "params": {
    "name": "analyze_repository",
    "arguments": {
      "repository_name": "My-Java-Project",
      "branch": "develop"
    }
  }
}
```

**Respuesta**:

```json
{
  "execution_id": "arn:aws:states:us-east-1:123456789012:execution:CodeReviewStateMachine:abc123"
}
```

### 2. Verificar Estado (Polling)

```json
POST /mcp/stream
{
  "method": "tools/call",
  "params": {
    "name": "check_status",
    "arguments": {
      "execution_id": "arn:aws:states:us-east-1:123456789012:execution:CodeReviewStateMachine:abc123"
    }
  }
}
```

**Respuesta (en progreso)**:

```json
{
  "status": "RUNNING"
}
```

**Respuesta (completado)**:

```json
{
  "status": "SUCCEEDED"
}
```

### 3. Obtener Resultados

```json
POST /mcp/stream
{
  "method": "tools/call",
  "params": {
    "name": "get_results",
    "arguments": {
      "execution_id": "arn:aws:states:us-east-1:123456789012:execution:CodeReviewStateMachine:abc123"
    }
  }
}
```

**Respuesta**:

```json
{
  "content": "Análisis completado exitosamente. Se encontraron 3 violaciones de Clean Architecture...",
  "file": "s3://code-review-results/abc123/analysis-report.json"
}
```

---

## ⚠️ Manejo de Errores

### Error: Tool no encontrado

```json
{
  "error": {
    "code": -32601,
    "message": "Tool not found: invalid_tool_name"
  }
}
```

### Error: Parámetros inválidos

```json
{
  "error": {
    "code": -32602,
    "message": "Invalid params: repository_name is required"
  }
}
```

### Error: Autenticación fallida

```json
{
  "error": {
    "code": 401,
    "message": "Unauthorized: Invalid API Key"
  }
}
```

---

## 🧪 Testing con Postman

### Configurar Postman

1. Crear nueva **Collection**: "MCP Server"
2. Agregar **Variable de entorno**:
    - `base_url`: `http://localhost:8080`
    - `api_key`: `dev-client.dev-secret-key-12345`

3. Crear **Pre-request Script** global:

```javascript
pm.request.headers.add({
  key: 'X-API-Key',
  value: pm.environment.get('api_key')
});
```

### Requests de Ejemplo

**1. Health Check**:

- Method: POST
- URL: `{{base_url}}/mcp/stream`
- Body:

```json
{
  "method": "tools/call",
  "params": {
    "name": "health",
    "arguments": {}
  }
}
```

**2. Analyze Repository**:

- Method: POST
- URL: `{{base_url}}/mcp/stream`
- Body:

```json
{
  "method": "tools/call",
  "params": {
    "name": "analyze_repository",
    "arguments": {
      "repository_name": "My-Java-Project",
      "branch": "develop"
    }
  }
}
```

---

**💡 Tip**: Todos los métodos MCP son reactivos y retornan `Mono<T>`. Los timeouts están configurados
a 30 segundos por defecto, pero pueden ajustarse en `application.yaml`.

Para más información:

- [Getting Started](getting-started.md)
- [Architecture](architecture.md)
- [Security](security.md)
