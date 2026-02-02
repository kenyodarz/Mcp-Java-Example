# TechDocs Templates 📚

Esta carpeta contiene plantillas base ("scaffolding") para crear documentación técnica de alta
calidad para servidores MCP y otros proyectos.

> **Objetivo**: Facilitar la creación de documentación estándar, explicativa y fácil de mantener ("
> For Dummies").

## 📁 Contenido

- `mkdocs.yml.template` - Configuración base de MkDocs.
- `docs/` - Plantillas de páginas Markdown:
  - `index.md.template` - Home / Overview.
  - `getting-started.md.template` - Guía de inicio paso a paso.
  - `architecture.md.template` - Diagramas y decisiones de diseño.
  - `api-reference.md.template` - Documentación de Tools y Prompts.
  - `security.md.template` - Guía de seguridad y autenticación.
  - `troubleshooting.md.template` - Solución de problemas comunes.

## 🚀 Cómo Usar

### 1. Copiar Templates

Copia el contenido de esta carpeta a la raíz de tu proyecto:

```bash
# PowerShell
Copy-Item -Path .techdocs-templates/mkdocs.yml.template -Destination ./mkdocs.yml
Copy-Item -Path .techdocs-templates/docs -Destination ./ -Recurse
```

### 2. Renombrar Archivos

Elimina la extensión `.template` de los archivos copiados en `docs/`:

```powershell
Get-ChildItem ./docs -Filter "*.template" | Rename-Item -NewName {$_.name -replace '.template',''}
```

### 3. Reemplazar Variables

Busca y reemplaza los siguientes placeholders en todos los archivos `.md` y `mkdocs.yml`:

| Variable           | Descripción              | Ejemplo                                   |
|--------------------|--------------------------|-------------------------------------------|
| `{{PROJECT_NAME}}` | Nombre del proyecto      | `Bancolombia MCP Server`                  |
| `{{DESCRIPTION}}`  | Descripción corta        | `Servidor MCP para consulta de saldos...` |
| `{{TEAM}}`         | Equipo responsable       | `Equipo de Arquitectura`                  |
| `{{TOOLS_COUNT}}`  | Cantidad de herramientas | `5`                                       |
| `{{FRAMEWORK}}`    | Tecnología base          | `Spring AI` / `FastMCP`                   |
| `{{REPO_URL}}`     | URL del repositorio git  | `https://github.com/org/repo`             |
| `{{JAVA_VERSION}}` | Versión de Java          | `17`                                      |
| `{{SERVER_URL}}`   | URL base del servidor    | `http://localhost:8080`                   |

### 4. Personalizar Contenido

Sigue las instrucciones marcadas con **📝 Instrucción** o **💡 Tip** dentro de cada archivo para
completar la información específica de tu proyecto.

## 📝 Estructura de Navegación

El archivo `mkdocs.yml` ya viene configurado con esta estructura estándar:

```yaml
nav:
  - Home: index.md
  - Getting Started: getting-started.md
  - Architecture: architecture.md
  - API Reference: api-reference.md
  - Security: security.md
  - Troubleshooting: troubleshooting.md
```

---

**💡 Tip**: Mantén la documentación simple. Es mejor tener poca documentación actualizada que mucha
desactualizada.
