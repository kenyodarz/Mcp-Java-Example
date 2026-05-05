# Code Review MCP Server

Bienvenido a la documentación técnica del **Code Review MCP Server** de Bancolombia.

## 🎯 ¿Qué es?

El Code Review MCP Server es un servidor basado en el **Model Context Protocol (MCP)** que permite
analizar repositorios completos de código y generar reportes estructurados con hallazgos,
sugerencias de mejora y recomendaciones de arquitectura.

**Características principales**:

- **🛡️ Análisis de Clean Architecture**: Valida el cumplimiento de las capas y reglas de dependencia
  en proyectos Java
- **☁️ Integración Azure DevOps**: Conexión nativa para leer repositorios y ramas desde Azure DevOps
- **🤖 IA Generativa**: Utiliza Claude 3.5 Sonnet a través de AWS Bedrock para análisis profundo del
  código
- **⚡ Procesamiento Asíncrono**: Implementado con AWS Step Functions para manejar análisis de larga
  duración
- **📊 Reportes Estructurados**: Entrega resultados detallados y accionables en formato JSON
- **🔐 Seguridad**: Autenticación con API Keys y auditoría completa de accesos

---

## 🎯 Importancia del Code Review Automatizado

### ¿Por qué es Crítico?

**Beneficios organizacionales**:

- **🚀 Acelera Code Reviews**: Análisis automático reduce tiempo de revisión manual
- **🔄 Consistencia**: Aplica las mismas reglas de arquitectura en todos los proyectos
- **📈 Mejora Calidad**: Detecta problemas arquitectónicos antes de producción
- **🛡️ Reduce Riesgos**: Identifica violaciones de Clean Architecture tempranamente
- **⚡ Aumenta Productividad**: Desarrolladores se enfocan en lógica de negocio, no en validaciones
  manuales

**Impacto en el desarrollo**:

- **🎯 Validación Arquitectónica**: Asegura que el código sigue Clean Architecture
- **🔗 Detección de Dependencias**: Identifica dependencias circulares y violaciones de capas
- **🧪 Análisis Semántico**: Entiende el contexto del código usando IA
- **🔧 Sugerencias Accionables**: Proporciona recomendaciones específicas de mejora
- **📋 Documentación de Decisiones**: Preserva el contexto de decisiones arquitectónicas

---

## 🏗️ Arquitectura

Este servidor sigue el patrón **Clean Architecture de Bancolombia** y está construido con:

- **Lenguaje**: Java 21+
- **Framework**: Spring Boot 4.x + Spring AI 2.0.0-M5
- **Protocolo**: MCP (Model Context Protocol) - Stateless HTTP
- **Stack Reactivo**: Spring WebFlux + Project Reactor
- **Infraestructura**: AWS (Step Functions, S3, Bedrock)
- **Integración**: Azure DevOps API

```
┌─────────────────────────────────────────────────────┐
│           Cliente MCP (Claude, Cursor, etc.)        │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP/SSE
                   ▼
┌─────────────────────────────────────────────────────┐
│            MCP Server (Spring AI 2.0.0-M5)          │
│  ┌───────────────────────────────────────────────┐  │
│  │  Entry Point: /mcp/stream (STATELESS/ASYNC)  │  │
│  └───────────────────────────────────────────────┘  │
│                                                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │   @McpTool  │  │ @McpResource │  │ @McpPrompt │ │
│  └─────────────┘  └──────────────┘  └────────────┘ │
│         │                │                  │        │
│         └────────────────┴──────────────────┘        │
│                          │                           │
│                          ▼                           │
│              ┌───────────────────────┐               │
│              │   Domain Use Cases    │               │
│              └───────────────────────┘               │
│                          │                           │
│                          ▼                           │
│              ┌───────────────────────┐               │
│              │  Driven Adapters      │               │
│              │  (Azure DevOps, AWS)  │               │
│              └───────────────────────┘               │
└─────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

Para comenzar a utilizar el servidor:

1. **[Instalación y Setup](getting-started.md)**: Configura el servidor localmente
2. **[API Reference](api-reference.md)**: Conoce las herramientas disponibles
3. **[Security](security.md)**: Configura autenticación con API Keys

### Ejemplo Rápido

```bash
# Analizar un repositorio
curl -X POST http://localhost:8080/mcp/stream \
  -H "X-API-Key: dev-client.dev-secret-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "tools/call",
    "params": {
      "name": "analyze_repository",
      "arguments": {
        "repository_name": "My-Java-Project",
        "branch": "develop"
      }
    }
  }'
```

---

## 📚 Capacidades MCP

El servidor expone las siguientes capacidades a través del protocolo MCP:

### 🔧 Tools (Herramientas)

- **`analyze_repository`**: Inicia análisis de código
- **`check_status`**: Consulta estado de análisis
- **`get_results`**: Obtiene resultados del análisis

### 📄 Resources (Recursos)

- **`system-info`**: Información del sistema
- **`user-info`**: Información de usuarios (template)

### 💬 Prompts (Plantillas)

- Plantillas de conversación predefinidas para interacción con IA

---

## 🔗 Integración con Clientes MCP

### Claude Desktop

```json
{
  "mcpServers": {
    "code-review-server": {
      "url": "http://localhost:8080/mcp/stream",
      "headers": {
        "X-API-Key": "claude-desktop.claude-secret-key-abcde"
      }
    }
  }
}
```

### Cursor / Windsurf

```json
{
  "mcp": {
    "servers": {
      "bancolombia": {
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

## 📊 Ambientes

El servidor está desplegado en los siguientes ambientes:

- **Desarrollo**:
  `https://inteligenciaartificial-int-dev.apps.ambientesbc.com/vsti-mcp/codereview/mcp`
- **QA**: `https://inteligenciaartificial-int-qa.apps.ambientesbc.com/vsti-mcp/codereview/mcp`
- **Producción**: `https://inteligenciaartificial-int.apps.bancolombia.com/vsti-mcp/codereview/mcp`

---

## 🤝 Contribución

Este proyecto es mantenido por el equipo de **Prácticas de Ingeniería de Software** de Bancolombia.

Para contribuir:

1. Sigue [Clean Architecture](architecture.md)
2. Implementa tests unitarios
3. Documenta con Javadoc
4. Crea un Pull Request en Azure DevOps

---

## 📞 Soporte

- **Azure DevOps
  **: [NU1041002_TI_MCPSERVERS_HB_MR](https://dev.azure.com/grupobancolombia/b267af7c-3233-4ad1-97b3-91083943100d/_git/NU1041002_TI_MCPSERVERS_HB_MR)
- **Pipeline
  **: [Build #55727](https://dev.azure.com/grupobancolombia/b267af7c-3233-4ad1-97b3-91083943100d/_build?definitionId=55727)
- **Equipo**: Prácticas de Ingeniería de Software

---

**💡 Tip**: La documentación vive junto al código. Cualquier cambio en el servidor debe incluir
actualización de docs en el mismo PR.

Para más detalles, explora las siguientes secciones:

- [Getting Started](getting-started.md)
- [Architecture](architecture.md)
- [API Reference](api-reference.md)
- [Security](security.md)
- [Troubleshooting](troubleshooting.md)
