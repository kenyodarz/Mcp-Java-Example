# Primeros Pasos

Esta guía te ayudará a configurar y comenzar a utilizar el Code Review MCP Server.

---

## 📋 Prerequisitos

Antes de comenzar, asegúrate de tener:

### Software Requerido

- **Java 17+**: JDK 17 o superior
- **Gradle 8.x**: Para compilar el proyecto
- **Git**: Para clonar el repositorio
- **Azure DevOps Access**: Permisos de lectura sobre repositorios

### Accesos Necesarios

- **Azure DevOps**: Token de acceso personal (PAT) con permisos de lectura
- **AWS** (para producción): Acceso a Step Functions, S3, y Bedrock
- **Cliente MCP**: Claude Desktop, Cursor, Windsurf, o cualquier cliente compatible

---

## 🚀 Instalación

### Opción A: Desde Maven Local (Desarrollo)

```bash
# 1. Clonar el repositorio
git clone https://dev.azure.com/grupobancolombia/b267af7c-3233-4ad1-97b3-91083943100d/_git/NU1041002_TI_MCPSERVERS_HB_MR
cd NU1041002_TI_MCPSERVERS_HB_MR

# 2. Compilar el proyecto
gradle clean build

# 3. Ejecutar el servidor
gradle :applications:app-service:bootRun
```

### Opción B: Usar JAR Pre-compilado

```bash
# 1. Compilar JAR
gradle :applications:app-service:bootJar

# 2. Ejecutar JAR
java -jar applications/app-service/build/libs/app-service.jar
```

### Verificar Instalación

```bash
# Health check
curl http://localhost:8080/actuator/health

# Respuesta esperada:
# {"status":"UP"}
```

---

## ⚙️ Configuración

### 1. Configurar application.yaml

El archivo `applications/app-service/src/main/resources/application.yaml` contiene la configuración
del servidor:

```yaml
server:
  port: 8080

spring:
  application:
    name: "mcp-bancolombia"

  ai:
    mcp:
      server:
        protocol: "STATELESS"
        name: "mcp-bancolombia"
        version: "1.0.0"
        type: "ASYNC"
        
        streamable-http:
          mcp-endpoint: "/mcp/stream"
        
        capabilities:
          tool: true
          resource: true
          prompt: true
        
        request-timeout: "30s"
```

### 2. Configurar API Keys

El servidor usa autenticación con API Keys. Las siguientes keys están predefinidas:

#### API Keys de Desarrollo

```
ID: dev-client
Secret: dev-secret-key-12345
Full Key: dev-client.dev-secret-key-12345
Expira: 1 año
```

#### API Keys de QA

```
ID: qa-client
Secret: qa-secret-key-67890
Full Key: qa-client.qa-secret-key-67890
Expira: 6 meses
```

#### API Keys para Claude Desktop

```
ID: claude-desktop
Secret: claude-secret-key-abcde
Full Key: claude-desktop.claude-secret-key-abcde
Expira: Nunca
```

Para más detalles sobre gestión de API Keys, consulta la [Guía de Seguridad](security.md).

### 3. Configurar Variables de Entorno (Opcional)

```bash
# Azure DevOps
export AZURE_DEVOPS_PAT="tu-personal-access-token"
export AZURE_DEVOPS_ORG="grupobancolombia"
export AZURE_DEVOPS_PROJECT="b267af7c-3233-4ad1-97b3-91083943100d"

# AWS (para producción)
export AWS_REGION="us-east-1"
export AWS_ACCESS_KEY_ID="tu-access-key"
export AWS_SECRET_ACCESS_KEY="tu-secret-key"
```

---

## 🔧 Configuración de Clientes MCP

### Claude Desktop

1. **Ubicar archivo de configuración:**
    - **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
    - **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
    - **Linux**: `~/.config/Claude/claude_desktop_config.json`

2. **Agregar configuración del servidor:**

```json
{
  "mcpServers": {
    "code-review-bancolombia": {
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

3. **Reiniciar Claude Desktop**

### Cursor / Windsurf

Edita el archivo de configuración de MCP:

```json
{
  "mcp": {
    "servers": {
      "bancolombia-code-review": {
        "url": "http://localhost:8080/mcp/stream",
        "headers": {
          "X-API-Key": "dev-client.dev-secret-key-12345"
        }
      }
    }
  }
}
```

---

## 🎯 Tu Primer Análisis

### Paso 1: Verificar Conexión

En Claude Desktop o tu cliente MCP, verifica que el servidor esté conectado:

```
¿Qué herramientas tienes disponibles?
```

Claude debería listar las herramientas MCP disponibles:

- `analyze_repository`
- `check_status`
- `get_results`
- `health`

### Paso 2: Iniciar Análisis

Solicita a Claude analizar un repositorio:

```
Por favor analiza el repositorio "My-Java-Project" en la rama "develop"
```

Claude ejecutará internamente:

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

**Respuesta esperada:**

```json
{
  "execution_id": "arn:aws:states:us-east-1:123456789012:execution:CodeReviewStateMachine:abc123"
}
```

### Paso 3: Verificar Estado

El análisis es asíncrono. Claude puede verificar el estado automáticamente:

```
¿Cuál es el estado del análisis?
```

**Estados posibles:**

- `RUNNING`: Análisis en progreso
- `SUCCEEDED`: Análisis completado exitosamente
- `FAILED`: Análisis falló

### Paso 4: Obtener Resultados

Una vez completado, Claude puede obtener los resultados:

```
Muéstrame los resultados del análisis
```

Claude presentará el reporte con:

- Hallazgos de arquitectura
- Violaciones de Clean Architecture
- Sugerencias de mejora
- Recomendaciones

---

## 🧪 Pruebas con cURL

### Listar Tools Disponibles

```bash
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/list"
  }'
```

### Ejecutar Health Check

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

### Analizar Repositorio

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

---

## 🐛 Troubleshooting Inicial

### Error: "Connection refused"

**Causa**: El servidor no está ejecutándose.

**Solución**:

```bash
gradle :applications:app-service:bootRun
```

### Error: "401 Unauthorized"

**Causa**: API Key inválida o faltante.

**Solución**: Verifica que el header `X-API-Key` esté presente y sea correcto:

```bash
X-API-Key: dev-client.dev-secret-key-12345
```

### Error: "Repository not found"

**Causa**: El repositorio no existe en Azure DevOps o no tienes permisos.

**Solución**:

1. Verifica el nombre exacto del repositorio en Azure DevOps
2. Confirma que tienes permisos de lectura
3. Verifica que el proyecto sea el correcto

Para más problemas comunes, consulta la [Guía de Troubleshooting](troubleshooting.md).

---

## 📚 Siguientes Pasos

Ahora que tienes el servidor funcionando:

1. **[Explora la Arquitectura](architecture.md)**: Entiende cómo funciona internamente
2. **[Consulta el API Reference](api-reference.md)**: Conoce todas las herramientas disponibles
3. **[Configura Seguridad](security.md)**: Gestiona API Keys y auditoría
4. **[Deployment](deployment.md)**: Despliega en QA o Producción

---

**💡 Tip**: Para desarrollo local, usa la API Key `dev-client.dev-secret-key-12345`. Para producción,
crea API Keys específicas con expiración configurada.
