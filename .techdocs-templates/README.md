# TechDocs Templates

Esta carpeta contiene plantillas base para crear documentación TechDocs en proyectos MCP.

## 📁 Contenido

- `mkdocs.yml.template` - Configuración base de MkDocs
- `docs/` - Plantillas de páginas Markdown
    - `index.md.template` - Página principal
    - `getting-started.md.template` - Guía de inicio
    - `architecture.md.template` - Documentación de arquitectura
    - `api-reference.md.template` - Referencia del API
    - `security.md.template` - Guía de seguridad
    - `troubleshooting.md.template` - Solución de problemas

## 🚀 Cómo Usar

### 1. Copiar Templates a tu Proyecto

```bash
# Copiar mkdocs.yml
cp .techdocs-templates/mkdocs.yml.template ./mkdocs.yml

# Copiar docs/
cp -r .techdocs-templates/docs ./docs
```

### 2. Personalizar

Reemplaza los placeholders en los archivos:

- `{{PROJECT_NAME}}` - Nombre de tu proyecto
- `{{DESCRIPTION}}` - Descripción breve
- `{{TEAM}}` - Nombre del equipo
- `{{TOOLS_COUNT}}` - Número de tools MCP
- `{{FRAMEWORK}}` - Framework usado (FastMCP, Spring AI, etc.)

### 3. Renombrar Archivos

```bash
# Remover extensión .template
cd docs
ren *.template *.
```

O en PowerShell:

```powershell
Get-ChildItem -Filter "*.template" | Rename-Item -NewName {$_.name -replace '.template',''}
```

### 4. Actualizar catalog-info.yaml

Agrega la anotación de TechDocs:

```yaml
metadata:
  annotations:
    backstage.io/techdocs-ref: dir:.
```

### 5. Previsualizar

```bash
npx @techdocs/cli serve
```

Abre `http://localhost:3000`

## 📝 Estructura Recomendada

### Para APIs/Servicios MCP

```
docs/
├── index.md              # Overview del servidor
├── getting-started.md    # Setup e instalación
├── architecture.md       # Clean Architecture, componentes
├── api-reference.md      # Tools, Resources, Prompts
├── security.md           # Autenticación, API Keys
└── troubleshooting.md    # Problemas comunes
```

### Para Librerías/SDKs

```
docs/
├── index.md              # Overview de la librería
├── installation.md       # Instalación
├── quick-start.md        # Primeros pasos
├── guides/
│   ├── basic-usage.md
│   └── advanced.md
├── api-reference/
│   └── classes.md
└── examples/
    └── simple.md
```

## ✨ Características de las Templates

- ✅ **Estructura TechDocs**: Sigue las mejores prácticas
- ✅ **Emojis**: Para mejor escaneabilidad
- ✅ **Ejemplos de código**: Con syntax highlighting
- ✅ **Enlaces internos**: Cross-references
- ✅ **Secciones estándar**: Consistentes entre proyectos

## 🎨 Personalización

### Cambiar Tema

En `mkdocs.yml`:

```yaml
theme:
  name: material
  palette:
    primary: 'indigo'  # Cambiar color
    accent: 'indigo'
```

### Agregar Plugins

```yaml
plugins:
  - techdocs-core
  - search
  - mermaid2  # Para diagramas
```

### Agregar Páginas

1. Crea el archivo `.md` en `docs/`
2. Agrégalo a `nav` en `mkdocs.yml`:

```yaml
nav:
  - Home: index.md
  - Nueva Página: nueva-pagina.md
```

## 📚 Recursos

- [Guía de Previsualización](../TECHDOCS_PREVIEW_GUIDE.md)
- [Documentación TechDocs](https://backstage.io/docs/features/techdocs/)
- [Guía de Estilo](../techdocs.md)

---

**💡 Tip**: Mantén la documentación simple y enfocada. Es mejor tener docs concisas y actualizadas
que extensas y obsoletas.
