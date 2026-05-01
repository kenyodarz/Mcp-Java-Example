# Guía de Previsualización de TechDocs

Esta guía te ayudará a previsualizar la documentación TechDocs localmente antes de hacer commit.

---

## 📋 Prerequisitos

- **Node.js 18+**: Para ejecutar `@techdocs/cli`
- **Docker** (Opción recomendada): Para usar la opción con Docker
- **Python 3.8+** (Opción alternativa): Para usar MkDocs directamente

---

## 🥇 Opción 1: Con Docker (Recomendada)

Esta es la opción más fácil y no requiere instalar MkDocs.

### 1. Verificar Docker

```bash
docker --version
```

Si no tienes Docker, descárgalo desde [docker.com](https://www.docker.com/products/docker-desktop)

### 2. Ejecutar TechDocs CLI

```bash
# Navegar al directorio del proyecto
cd c:\Users\minaj\Work\Bancolombia\Mcp_Server\mcp

# Servir la documentación (usa Docker automáticamente)
npx @techdocs/cli serve

# O especificar el puerto
npx @techdocs/cli serve --port 8001
```

### 3. Abrir en el Navegador

Abre `http://localhost:3000` (o el puerto que especificaste)

### 4. Ver Cambios en Tiempo Real

El servidor se recarga automáticamente cuando editas archivos Markdown en `docs/`.

---

## 🥈 Opción 2: Sin Docker (Instalando MkDocs)

Si no puedes o no quieres usar Docker, puedes instalar MkDocs directamente.

### 1. Verificar Python

```bash
python --version
```

**Debe ser 3.8+**

Si no tienes Python:

- Instálalo desde [Microsoft Store](https://apps.microsoft.com/detail/9NRWMJP3717K)
  o [python.org](https://www.python.org/downloads/)
- ✅ Marca **"Add Python to PATH"** durante la instalación

### 2. Instalar MkDocs + Plugins Requeridos

```bash
pip install mkdocs mkdocs-material mkdocs-techdocs-core
```

Verificar instalación:

```bash
mkdocs --version
```

### 3. Ejecutar TechDocs CLI sin Docker

```bash
# Navegar al directorio del proyecto
cd c:\Users\minaj\Work\Bancolombia\Mcp_Server\mcp

# Servir sin Docker
npx @techdocs/cli serve --no-docker

# O especificar puerto
npx @techdocs/cli serve --no-docker --port 8001
```

### 4. Abrir en el Navegador

Abre `http://localhost:3000`

---

## 🔧 Comandos Útiles

### Generar Documentación Estática

```bash
# Generar HTML estático en site/
npx @techdocs/cli generate

# Generar sin Docker
npx @techdocs/cli generate --no-docker
```

### Publicar a Storage (Producción)

```bash
# Publicar a S3
npx @techdocs/cli publish --publisher-type awsS3 --storage-name <bucket-name>

# Publicar a Azure Blob Storage
npx @techdocs/cli publish --publisher-type azureBlobStorage --storage-name <container-name>
```

---

## 🐛 Troubleshooting

### Error: "Docker is not running"

**Solución**: Inicia Docker Desktop

```bash
# Verificar que Docker está corriendo
docker ps
```

### Error: "mkdocs: command not found"

**Solución**: Instala MkDocs

```bash
pip install mkdocs mkdocs-material mkdocs-techdocs-core
```

### Error: "Module 'mkdocs_techdocs_core' not found"

**Solución**: Reinstala el plugin

```bash
pip install --upgrade mkdocs-techdocs-core
```

### La documentación no se actualiza

**Solución**:

1. Detén el servidor (Ctrl+C)
2. Limpia el cache: `rm -rf site/`
3. Vuelve a ejecutar: `npx @techdocs/cli serve`

### Puerto ya en uso

**Solución**: Usa otro puerto

```bash
npx @techdocs/cli serve --port 8001
```

---

## 📊 Estructura de Archivos

Para que TechDocs funcione correctamente, asegúrate de tener esta estructura:

```
tu-proyecto/
├── catalog-info.yaml          ← Con backstage.io/techdocs-ref: dir:.
├── mkdocs.yml                 ← Configuración de MkDocs
└── docs/                      ← Archivos Markdown
    ├── index.md              ← Página principal (requerida)
    ├── getting-started.md
    ├── architecture.md
    ├── api-reference.md
    └── ...
```

---

## 🚀 CI/CD Automático

En producción, la documentación se publica automáticamente cuando haces push:

1. **Azure DevOps Pipeline** detecta cambios en `docs/` o `mkdocs.yml`
2. **TechDocs CLI** genera la documentación
3. **Publica** a Azure Blob Storage / S3
4. **Backstage** muestra la documentación en el tab "Docs"

No necesitas hacer nada manual, solo hacer commit de tus cambios.

---

## ✅ Checklist de Verificación

Antes de hacer commit, verifica:

- [ ] `npx @techdocs/cli serve` funciona sin errores
- [ ] Todos los enlaces internos funcionan
- [ ] Las imágenes se muestran correctamente
- [ ] El código de ejemplo tiene syntax highlighting
- [ ] La navegación en `mkdocs.yml` es correcta
- [ ] `catalog-info.yaml` tiene `backstage.io/techdocs-ref: dir:.`

---

## 📚 Recursos Adicionales

- [Documentación oficial de TechDocs](https://backstage.io/docs/features/techdocs/)
- [TechDocs CLI](https://backstage.io/docs/features/techdocs/cli/)
- [MkDocs Documentation](https://www.mkdocs.org/)
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

---

**💡 Tip**: Usa la opción con Docker para evitar problemas de dependencias. Es más rápida y
confiable.
