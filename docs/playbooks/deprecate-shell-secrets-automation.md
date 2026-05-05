# Playbook: Deprecar Automatización Shell de Secretos

> **Tipo**: Refactoring Playbook  
> **Aplica a**: Proyectos con Ministack + `docker-entrypoint-initaws.d/` + scripts shell de init  
> **Problema raíz**: Scripts shell que crean secretos automáticamente fallan silenciosamente, sin
> logs  
> **Solución**: Reemplazar la automatización por documentación de comandos manuales explícitos

---

## 🎯 Objetivo

Dado un proyecto que usa scripts shell para inicializar secretos en Ministack/LocalStack durante
el arranque del contenedor, este playbook guía al agente para:

1. Identificar todos los scripts de automatización involucrados
2. Deprecar el init script sin eliminarlo (trazabilidad)
3. Desactivar el mount del script en `docker-compose.yml`
4. Crear un documento markdown con los comandos manuales equivalentes
5. Actualizar la documentación existente para referenciar el nuevo manual

---

## 📋 Pre-condiciones

Verificar que el proyecto tiene al menos uno de estos patrones antes de proceder:

```text
[x] docker-compose.yml con un volume: ./docker/init-*.sh:/docker-entrypoint-initaws.d/
[x] docker/ con un script shell de creación de secretos
[x] scripts/ con un script wrapper que llama a los comandos AWS
```

Si ninguno de los tres existe, este playbook **no aplica**.

---

## 🗂️ Fase 1 — Exploración

### 1.1 Localizar el init script

```
Buscar archivos: docker/init-*.sh
Buscar archivos: scripts/*.sh
Buscar en docker-compose.yml: docker-entrypoint-initaws.d
```

### 1.2 Leer el contenido completo de cada script encontrado

Identificar y registrar:

| Ítem                               | Valor encontrado                             |
|------------------------------------|----------------------------------------------|
| Ruta del init script               | e.g. `docker/init-secrets.sh`                |
| Nombre del servicio docker-compose | e.g. `ministack`                             |
| Endpoint del emulador              | e.g. `http://localhost:4566`                 |
| Lista de secretos creados          | e.g. `api-key`, `client-id`, `client-secret` |
| Valores dev de cada secreto        | e.g. `"my-dev-value"`                        |
| Descriptions de cada secreto       | e.g. `"API Key para..."`                     |
| Existe script wrapper (scripts/)   | Sí / No                                      |

### 1.3 Localizar documentación existente a actualizar

```
Buscar archivos: docs/guides/*.md
Buscar texto: init-secrets  (en archivos .md y .yml)
```

---

## 🔧 Fase 2 — Cambios al código

### 2.1 Deprecar el init script (NO eliminar)

Reemplazar **todo el contenido** del script con una cabecera de deprecación:

```bash
#!/bin/bash

# ============================================
# ⚠️  DEPRECATED - Este script ha sido deprecado
# ============================================
# Este script ya no se utiliza. Para crear secretos,
# siga las instrucciones en: docs/guides/manual-secrets-setup.md
#
# Razón de deprecación:
# - Falla silenciosamente sin logs visibles
# - No proporciona visibilidad para debugging
# - Reemplazado por comandos manuales explícitos
#
# Opciones de reemplazo:
#
#   Opción 1 (AWS CLI local):
#     export AWS_ENDPOINT=http://localhost:4566
#     aws secretsmanager create-secret \
#       --name <SECRET_NAME> \
#       --secret-string "<VALUE>" \
#       --endpoint-url "$AWS_ENDPOINT" --region us-east-1
#
#   Opción 2 (Docker Compose exec):
#     docker compose exec -T <SERVICE> aws secretsmanager create-secret \
#       --name <SECRET_NAME> \
#       --secret-string "<VALUE>" \
#       --endpoint-url "http://localhost:4566" --region us-east-1
#
# Ver guía completa: docs/guides/manual-secrets-setup.md
# ============================================

exit 0
```

> **Regla**: Mantener el archivo. No eliminar. Permite trazabilidad del cambio.

---

### 2.2 Comentar el volume mount en docker-compose.yml

Localizar la línea con el mount del init script (bajo `volumes:` del servicio emulador) y
comentarla:

```yaml
# ANTES
volumes:
  - emulator-data:/tmp/data
  - ./docker/init-secrets.sh:/docker-entrypoint-initaws.d/init-secrets.sh

# DESPUÉS
volumes:
  - emulator-data:/tmp/data
  # DEPRECATED: Automatización shell removida. Ver: docs/guides/manual-secrets-setup.md
  # - ./docker/init-secrets.sh:/docker-entrypoint-initaws.d/init-secrets.sh
```

---

## 📄 Fase 3 — Crear documento de comandos manuales

Crear el archivo `docs/guides/manual-secrets-setup.md` con esta estructura exacta,
adaptando los valores del inventario levantado en Fase 1:

---

### Plantilla: `docs/guides/manual-secrets-setup.md`

````markdown
# Configuración Manual de Secretos en <EMULADOR>

> **Nota**: Este documento reemplaza el script automatizado `<RUTA_SCRIPT>`,
> deprecado por fallos silenciosos sin logs.

## Requisitos previos

- Docker y Docker Compose instalados
- AWS CLI instalado (o usar Opción 2 sin AWS CLI local)
- Emulador iniciado: `docker compose up -d`

## Verificar que el emulador está corriendo

```bash
docker compose ps
curl http://localhost:<PUERTO>/<HEALTH_PATH>
```

Resultado esperado: `{"status": "ok"}`

---

## Opción 1: AWS CLI local (Recomendado)

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=<REGION>
export AWS_ENDPOINT=http://localhost:<PUERTO>

# Por cada secreto del inventario:
aws secretsmanager create-secret \
    --name <NOMBRE_SECRETO> \
    --secret-string "<VALOR_DEV>" \
    --description "<DESCRIPCION>" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region <REGION>
```

---

## Opción 2: Docker Compose exec (sin AWS CLI local)

```bash
export AWS_ENDPOINT=http://localhost:<PUERTO>

# Por cada secreto del inventario:
docker compose exec -T <SERVICIO> aws secretsmanager create-secret \
    --name <NOMBRE_SECRETO> \
    --secret-string "<VALOR_DEV>" \
    --description "<DESCRIPCION>" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region <REGION>
```

> El flag `-T` deshabilita pseudo-terminal. Necesario en CI/CD.

---

## Verificar los secretos

```bash
aws secretsmanager list-secrets \
    --query 'SecretList[*].[Name,Description]' \
    --output table \
    --endpoint-url "http://localhost:<PUERTO>" \
    --region <REGION>
```

## Operaciones adicionales

### Actualizar un secreto

```bash
aws secretsmanager update-secret \
    --secret-id <NOMBRE_SECRETO> \
    --secret-string "nuevo-valor" \
    --endpoint-url "http://localhost:<PUERTO>" \
    --region <REGION>
```

### Eliminar un secreto

```bash
aws secretsmanager delete-secret \
    --secret-id <NOMBRE_SECRETO> \
    --force-delete-without-recovery \
    --endpoint-url "http://localhost:<PUERTO>" \
    --region <REGION>
```

---

## Solución de problemas

| Problema | Causa | Solución |
|----------|-------|---------|
| `unable to connect` | Emulador no está corriendo | `docker compose up -d && sleep 15` |
| `ResourceExistsException` | Secreto ya existe | Usar `update-secret` en vez de `create-secret` |
| `command not found: aws` | AWS CLI no instalado | Usar Opción 2 (Docker exec) |

---

## Referencia de secretos

| Secreto | Descripción | Valor dev |
|---------|-------------|-----------|
| `<NOMBRE>` | `<DESCRIPCION>` | `<VALOR_DEV>` |

---

**Última actualización**: <FECHA>
````

---

## 📝 Fase 4 — Actualizar documentación existente

Para cada archivo `.md` encontrado que mencione el init script:

### Patrón de actualización

1. **Buscar** la sección que describe la inicialización automática (e.g.
   `## Inicialización automática`)
2. **Reemplazarla** con:

```markdown
## Inicialización de Secretos

### ⚠️ DEPRECADO: Inicialización automática

El script `<RUTA_SCRIPT>` ha sido **deprecado** — falla silenciosamente sin logs visibles.

**Para crear secretos, siga la guía**: 📖 [
`docs/guides/manual-secrets-setup.md`](./manual-secrets-setup.md)

### Opciones rápidas

#### Opción 1: AWS CLI local

```bash
export AWS_ENDPOINT=http://localhost:<PUERTO>
aws secretsmanager create-secret \
  --name <NOMBRE> --secret-string "<VALOR>" \
  --endpoint-url "$AWS_ENDPOINT" --region <REGION>
```

#### Opción 2: Docker Compose

```bash
docker compose exec -T <SERVICIO> aws secretsmanager create-secret \
  --name <NOMBRE> --secret-string "<VALOR>" \
  --endpoint-url "http://localhost:<PUERTO>" --region <REGION>
```

```

3. **Agregar link** en secciones de "Archivos involucrados":

```markdown
/docker/init-secrets.sh    (DEPRECADO — ver docs/guides/manual-secrets-setup.md)
```

---

## ✅ Checklist de Definición de Done

Al terminar verificar:

- [ ] `docker/init-<nombre>.sh` contiene SOLO la cabecera de deprecación + `exit 0`
- [ ] `docker-compose.yml`: volume mount del script está comentado con nota DEPRECATED
- [ ] `docs/guides/manual-secrets-setup.md` existe y tiene las 3 opciones de comandos
- [ ] Todos los `.md` que mencionaban el init script fueron actualizados con link al nuevo manual
- [ ] `docker compose up -d` funciona sin errores (el comentario no rompe nada)
- [ ] Los comandos manuales del nuevo documento son ejecutables y crean los secretos correctamente

---

## 🔁 Prompt para Agente IA

Copiar y pegar este prompt al iniciar el trabajo en un nuevo proyecto:

---

```
Contexto: Este proyecto usa un script shell (`docker/init-secrets.sh` o similar) montado
en `/docker-entrypoint-initaws.d/` para crear secretos automáticamente al arrancar
Ministack/LocalStack. El script falla silenciosamente.

Tarea: Aplicar el playbook `docs/playbooks/deprecate-shell-secrets-automation.md` en este proyecto.

Pasos obligatorios:
1. Lee `docker-compose.yml` y cualquier script en `docker/` y `scripts/` para hacer inventario
   completo de: ruta del script, nombre del servicio, endpoint, puerto, región, lista de secretos
   (nombre, valor, descripción).
2. Reemplaza el contenido de `docker/init-secrets.sh` (o el equivalente) con SOLO
   una cabecera de deprecación + `exit 0`. NO eliminar el archivo.
3. Comenta el volume mount del script en `docker-compose.yml` con una nota DEPRECATED
   que referencie `docs/guides/manual-secrets-setup.md`.
4. Crea `docs/guides/manual-secrets-setup.md` con:
   - Sección de requisitos previos
   - Opción 1: comandos AWS CLI local (con todos los secretos del inventario)
   - Opción 2: comandos Docker Compose exec (con todos los secretos del inventario)
   - Sección de verificación (list-secrets)
   - Operaciones adicionales (update, delete)
   - Tabla de solución de problemas
   - Tabla resumen de secretos con nombres, descripciones y valores dev
5. Actualiza cualquier `.md` en `docs/` que mencione el init script para referenciar
   el nuevo manual en vez de la automatización.

Restricciones:
- NO eliminar ningún archivo .sh existente
- NO cambiar el comportamiento de docker-compose.yml más allá de comentar ese volume
- NO hardcodear URLs o puertos que difieran de lo que ya usa el proyecto
- Adaptar todos los valores al inventario real del proyecto (no copiar valores de ejemplo)
```

---

## 📚 Referencias

- Playbook original aplicado en: `mcp-server-performance-tests/mcp`
- Documento resultante: `docs/guides/manual-secrets-setup.md`
- Guía de Ministack: `docs/guides/ministack-setup.md`
- SDD Framework: `docs/SDD/README.md`

