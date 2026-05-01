---
id: plugin-techdocs
title: Plugin TechDocs
description: Documentación técnica que vive junto al código en Kaizen
---

![TechDocs Plugin](../assets/techdocs/techdocs-plugin.png)

El **Plugin TechDocs** es una de las funcionalidades más importantes de Kaizen, diseñado para que la
documentación técnica viva junto al código y se mantenga siempre actualizada. **La documentación
bien estructurada es fundamental para el éxito de cualquier proyecto de software**, ya que facilita
la adopción, reduce el tiempo de onboarding y mejora la mantenibilidad del código.

## 🎯 Importancia de la Documentación

### ¿Por qué Documentar es Crítico?

**Beneficios organizacionales**:

- **🚀 Acelera Onboarding**: Nuevos desarrolladores entienden rápidamente el proyecto
- **🔄 Facilita Mantenimiento**: El conocimiento no se pierde cuando cambian los equipos
- **📈 Mejora Adopción**: Componentes bien documentados son más utilizados
- **🛡️ Reduce Riesgos**: Menor dependencia del conocimiento tácito
- **⚡ Aumenta Productividad**: Menos tiempo buscando información, más tiempo desarrollando

**Impacto en el desarrollo**:

- **🎯 Claridad de Propósito**: Define claramente qué hace y cómo usar cada componente
- **🔗 APIs Comprensibles**: Facilita la integración entre servicios
- **🧪 Testing Efectivo**: Documenta casos de uso y escenarios de prueba
- **🔧 Troubleshooting**: Guías para resolver problemas comunes
- **📋 Decisiones Arquitectónicas**: Preserva el contexto de decisiones técnicas

---

## 📚 Principio: Documentación Junto al Código

### Docs-as-Code Philosophy

**TechDocs implementa el principio fundamental**: **La documentación debe vivir junto al repositorio
del componente de software**

**Ventajas de este enfoque**:

- **🔄 Sincronización**: Cambios en código y docs en el mismo PR
- **📝 Versionado**: La documentación sigue el mismo versionado que el código
- **👥 Ownership**: El mismo equipo mantiene código y documentación
- **🔍 Revisión**: Docs pasan por el mismo proceso de code review
- **🚀 CI/CD**: Documentación se publica automáticamente con cada release

### Estructura Recomendada

**En cada repositorio de componente**:

```
mi-componente/
├── src/                  # Código fuente
├── docs/                 # Documentación TechDocs
│   ├── index.md         # Página principal
│   ├── getting-started.md
│   ├── api-reference.md
│   └── troubleshooting.md
├── mkdocs.yml           # Configuración MkDocs
└── catalog-info.yaml    # Entity descriptor
```

---

## 🛠️ Tecnología: MkDocs + TechDocs CLI

### MkDocs como Motor

**TechDocs utiliza MkDocs** como generador de documentación estática:

**Características de MkDocs**:

- **📝 Markdown**: Sintaxis simple y familiar para desarrolladores
- **🎨 Temas**: Material Design theme por defecto
- **🔍 Búsqueda**: Búsqueda integrada en la documentación
- **📱 Responsive**: Optimizado para dispositivos móviles
- **🔗 Navegación**: Estructura jerárquica automática

**Archivo `mkdocs.yml` básico**:

```yaml
site_name: 'Mi Componente'
site_description: 'Documentación técnica del componente'

nav:
  - Home: index.md
  - Getting Started: getting-started.md
  - API Reference: api-reference.md
  - Troubleshooting: troubleshooting.md

plugins:
  - techdocs-core
```

### TechDocs CLI de Backstage

**El CLI de TechDocs** proporciona herramientas para desarrollar y publicar documentación:

#### Comandos Principales

**Desarrollo local**:

```bash
# Servir documentación localmente
techdocs-cli serve

# Generar documentación estática
techdocs-cli generate

# Generar con Docker (ambiente controlado)
techdocs-cli generate --docker

# Publicar a storage (S3, GCS, Azure)
techdocs-cli publish --publisher-type awsS3
```

**Workflow típico**:

1. **✏️ Editar** archivos Markdown en `/docs`
2. **👀 Preview** con `techdocs-cli serve`
3. **🔄 Iterar** hasta obtener resultado deseado
4. **📤 Commit** cambios junto con código
5. **🚀 CI/CD** publica automáticamente

---

## 📖 Creación de Documentación para Componentes

### Recordando el Ejemplo del Index

**Como se vio en la página de introducción**, crear documentación es un proceso sencillo pero
estructurado:

#### 1. Configurar Entity Descriptor

**En `catalog-info.yaml`**:

```yaml
apiVersion: backstage.io/v1alpha1
kind: Component
metadata:
  name: mi-componente
  annotations:
    backstage.io/techdocs-ref: dir:.
spec:
  type: service
  lifecycle: production
  owner: mi-equipo
```

#### 2. Crear Estructura de Docs

**Directorio `/docs` con archivos base**:

**`docs/index.md`** - Página principal:

```markdown
# Mi Componente

Bienvenido a la documentación técnica de Mi Componente.

## ¿Qué es?

Mi Componente es un servicio que...

## Quick Start

Para comenzar a usar este componente:

1. Instalar dependencias
2. Configurar variables
3. Ejecutar aplicación
```

#### 3. Configurar MkDocs

**`mkdocs.yml`** en la raíz del proyecto:

```yaml
site_name: 'Mi Componente - Documentación'
site_description: 'Documentación técnica completa'

nav:
  - Inicio: index.md
  - Primeros Pasos: getting-started.md
  - API: api-reference.md
  - Solución de Problemas: troubleshooting.md

plugins:
  - techdocs-core

theme:
  name: material
  palette:
    primary: 'blue'
    accent: 'blue'
```

#### 4. Desarrollar Contenido

**Iterar con preview local**:

```bash
# En el directorio del proyecto
techdocs-cli serve --mkdocs-port 8001

# Abrir http://localhost:8001 para preview
```

---

## 🔧 Funcionalidades del Plugin TechDocs

### 📚 Navegación y Descubrimiento

![TechDocs Navigation](../assets/techdocs/techdocs-navigation.png)

**Acceso a documentación**:

- **📋 Desde Entity Page**: Tab "Docs" en cada componente
- **🔍 Búsqueda Global**: Encontrar docs desde el buscador principal
- **📚 Catálogo de Docs**: Vista dedicada de toda la documentación
- **⭐ Favoritos**: Acceso rápido a documentación marcada

### 🎨 Renderizado y Presentación

**Características visuales**:

- **🎨 Material Theme**: Diseño moderno y profesional
- **📱 Responsive**: Optimizado para todos los dispositivos
- **🔍 Búsqueda Integrada**: Search dentro de cada documento
- **🔗 Enlaces Automáticos**: Cross-references entre documentos
- **📊 Diagramas**: Soporte para Mermaid, PlantUML
- **💻 Code Highlighting**: Syntax highlighting para múltiples lenguajes

### 🔄 Sincronización Automática

**Actualización continua**:

- **⚡ Build Automático**: Cada push actualiza la documentación
- **🔄 Sync con Repositorio**: Siempre en sync con el código
- **📅 Timestamp**: Muestra fecha de última actualización
- **👤 Contributor Info**: Información de quién hizo cambios

---

## 📊 Gestión y Organización

### 📁 Organización de Documentación

**Estructura recomendada por tipo de componente**:

#### Para APIs/Servicios

```markdown
docs/
├── index.md # Overview del servicio
├── getting-started.md # Setup y quick start
├── api/
│ ├── authentication.md # Autenticación
│ ├── endpoints.md # Descripción de endpoints
│ └── examples.md # Ejemplos de uso
├── deployment/
│ ├── local.md # Desarrollo local
│ ├── staging.md # Deploy a staging
│ └── production.md # Deploy a producción
└── troubleshooting.md # Solución de problemas
```

#### Para Librerías/SDKs

```markdown
docs/
├── index.md # Overview de la librería
├── installation.md # Instalación
├── quick-start.md # Primeros pasos
├── guides/
│ ├── basic-usage.md # Uso básico
│ ├── advanced.md # Funcionalidades avanzadas
│ └── best-practices.md # Mejores prácticas
├── api-reference/
│ ├── classes.md # Documentación de clases
│ ├── methods.md # Métodos disponibles
│ └── types.md # Tipos y interfaces
└── examples/
├── simple.md # Ejemplos simples
└── complex.md # Casos de uso complejos
```

---

## 🚀 Casos de Uso y Ejemplos

### Para Desarrolladores de Backend

**Documentar un microservicio**:

1. **📋 Overview**: Propósito, arquitectura, responsabilidades
2. **🔧 Setup**: Requisitos, instalación, configuración
3. **🔌 API**: Endpoints, request/response, autenticación
4. **🚀 Deployment**: Docker, Kubernetes, variables de entorno
5. **🔍 Monitoring**: Logs, métricas, health checks
6. **🛠️ Troubleshooting**: Problemas comunes y soluciones

### Para Desarrolladores Frontend

**Documentar una aplicación web**:

1. **🎯 Overview**: Funcionalidad, tecnologías, arquitectura
2. **⚡ Quick Start**: Setup de desarrollo local
3. **🎨 UI Components**: Librería de componentes
4. **🔗 API Integration**: Cómo consumir servicios backend
5. **🧪 Testing**: Unit tests, integration tests, E2E
6. **📦 Build & Deploy**: Proceso de construcción y despliegue

### Para Equipos de DevOps

**Documentar infraestructura**:

1. **☁️ Architecture**: Diagramas de infraestructura
2. **🔧 Provisioning**: Terraform, CloudFormation
3. **📊 Monitoring**: Dashboards, alertas, SLIs/SLOs
4. **🔐 Security**: Políticas, compliance, access control
5. **💾 Backup & Recovery**: Estrategias de respaldo
6. **📋 Runbooks**: Procedimientos operacionales

---

## 🔗 Integración con Otros Plugins

### 📚 Software Catalog

- **🔄 Auto-discovery**: Documentación aparece automáticamente en entity pages
- **🏷️ Metadata Sharing**: Tags y propiedades compartidas
- **🔗 Cross-references**: Enlaces entre componentes relacionados

### 🔍 Search Plugin

- **🔎 Full-text Search**: Contenido de docs indexado globalmente
- **📊 Results Ranking**: Documentación priorizada en resultados
- **🎯 Contextual Search**: Búsqueda dentro de documentación específica

### 🛡️ OpEx Plugin

- **📋 Documentation Quality**: Métricas de calidad de documentación
- **✅ Compliance Checks**: Validación de documentación requerida
- **📊 Coverage Reports**: Componentes sin documentación

### 🚀 Plexo Plugin

- **🏗️ Template Integration**: Templates incluyen estructura de docs
- **📄 Auto-generation**: Generación automática de docs base
- **🔄 Lifecycle Management**: Docs actualizadas con cambios de infraestructura

---

## 💡 Tips y Mejores Prácticas

### 📝 Escribiendo Documentación Efectiva

**Principios fundamentales**:

- ✅ **Comienza por el "por qué"**: Explica el propósito antes del "cómo"
- ✅ **Estructura clara**: Usa headers, listas y secciones lógicas
- ✅ **Ejemplos prácticos**: Incluye código real y casos de uso
- ✅ **Mantente actualizado**: Docs obsoletas son peor que no tener docs
- ✅ **Audiencia específica**: Escribe para tu usuario objetivo

**Estructura recomendada para cada página**:

1. **🎯 Objetivo**: Qué aprenderá el lector
2. **📋 Prerequisitos**: Qué necesita saber/tener antes
3. **🔧 Implementación**: Pasos detallados con ejemplos
4. **✅ Verificación**: Cómo confirmar que funcionó
5. **🔗 Siguientes pasos**: Hacia dónde ir después

### 🔄 Mantenimiento Continuo

**Keeping docs alive**:

- ✅ **Review regular**: Incluye docs en definition of done
- ✅ **Update con features**: Documenta nuevas funcionalidades
- ✅ **Fix broken links**: Usa herramientas para detectar links rotos
- ✅ **User feedback**: Recoge comentarios de usuarios reales
- ✅ **Deprecation notices**: Marca claramente funcionalidad obsoleta

### 🛠️ Herramientas y Automation

**Mejorando el workflow**:

- ✅ **Preview automático**: CI que genera preview de cambios
- ✅ **Link checking**: Validación automática de enlaces
- ✅ **Spell checking**: Verificación ortográfica automática
- ✅ **Template enforcement**: Templates para tipos de documentación
- ✅ **Metrics tracking**: Métricas de uso y engagement

---

**💡 Tip**: La documentación en TechDocs no es solo un "nice to have" - es una inversión en la
productividad futura de tu equipo. Un componente bien documentado se adopta más rápido, requiere
menos soporte y genera menos fricción en el desarrollo.

Para explorar más funcionalidades de Kaizen, visita la [documentación del Home](../home/home.md) o
usa el [Plugin de Search](../search/plugin-search.md) para encontrar documentación específica.
