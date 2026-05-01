# Playbook de Validación y Refactorización MCP (Bancolombia SDD)

Este instructivo te guía para validar, limpiar y refactorizar un proyecto MCP ya generado a partir
de contratos Postman/OpenAPI, asegurando cumplimiento estricto de Clean Architecture, seguridad y
convenciones del workflow "agent-api-to-mcp-workflow" del framework SDD de Bancolombia.

**Repository**: https://github.com/kenyodarz/bancolombia-api-sdd-framework-export

## Stack Tecnológico

| Componente                                | Versión  |
|-------------------------------------------|----------|
| Java                                      | 25       |
| Spring Boot                               | 4.x      |
| Jackson                                   | 3        |
| Spring AI                                 | 2.0.0 M4 |
| Gradle                                    | 9.5.0    |
| Bancolombia Clean Architecture Scaffolder | 4.4.1    |

---

## 1. Inyección y Validación del Framework SDD (CRÍTICO)

Sigue las instrucciones del archivo `../../init-bancolombia-sdd.md` para:

- Clonar e inyectar el framework SDD de Bancolombia desde:
  `https://github.com/kenyodarz/bancolombia-api-sdd-framework-export`
- Validar la existencia de TODOS los archivos y carpetas requeridas:
    - `docs/playbooks/agent-api-to-mcp-workflow.md` ✓ CRÍTICO
    - `docs/playbooks/scaffold-baseline.md`
    - `docs/playbooks/security-baseline.md`
    - `docs/templates/api-contract-to-mcp.md`
    - `overlays/api-to-mcp/README.md`
    - `overlays/mcp-security-entra-id/README.md`
    - `skills/` (directorio completo)

**DETENER EL PROCESO si falta algún archivo o directorio crítico.**

---

## 2. Comprensión Obligatoria del Playbook

Lee y comprende estos documentos en orden:

1. **agent-api-to-mcp-workflow.md**  
   Define las 5 fases de implementación:
    - **Fase 1 (Intake)**: Parsear contratos, entender baseline, validar especificaciones
    - **Fase 2 (Scaffolding)**: Generar estructura usando comandos Gradle
    - **Fase 3 (Refinement)**: Implementar Clean Architecture correctamente
    - **Fase 4 (Security)**: Aplicar políticas de seguridad fail-closed
    - **Fase 5 (Cleanup)**: Remover placeholders, validar y generar reportes

2. **scaffold-baseline.md**  
   Define qué estructura genera el Scaffolder y qué requiere refinamiento manual.

3. **security-baseline.md**  
   Define políticas de seguridad, autenticación y autorización en todos los niveles.

4. **api-contract-to-mcp.md** (Template)  
   Matriz de mapeo entre operaciones HTTP y MCP Primitives (Tool/Resource/Prompt).

---

## 3. Validación de Tools MCP (Iterativa por Método HTTP)

Para cada método HTTP encontrado en los contratos (Postman/OpenAPI):

✓ **Debe existir exactamente 1 Tool MCP, 1 UseCase y 1 Adapter** (Granularidad 1:1)

- **MCP Tool (Entry Point)**
    - Delega SOLO al UseCase, sin lógica de dominio ni acceso a red
    - Contiene anotación: `@McpTool(name = "...", description = "...")`
    - Ubicación:
      `infrastructure/entry-points/mcp-server/src/main/java/co/com/bancolombia/mcp/tools/`

- **Use Case (Domain Layer)**
    - Orquestación de dominio únicamente
    - Sin intentos HTTP, sin WebClient, sin RestTemplate
    - Delega integración al Driven Adapter
    - Ubicación: `domain/usecase/src/main/java/co/com/bancolombia/usecase/`

- **Driven Adapter (Infrastructure)**
    - ÚNICO responsable de integración HTTP con APIs externas
    - Maneja errores de red, timeouts, reintentos
    - Lee URLs desde `application.yaml` (NO hardcoding)
    - Ubicación:
      `infrastructure/driven-adapters/rest-consumer/src/main/java/co/com/bancolombia/adapter/`

---

## 4. Seguridad: Política "Fail-Closed" (OBLIGATORIO)

✓ **Entra ID + OAuth2**

- Todo acceso sin token válido debe ser RECHAZADO por defecto
- Aplicar `@PreAuthorize("hasRole('MCP.CAPABILITY.NAME')")`  en cada Tool
- Validar que claims requeridos existan en el token
- Si claims faltan → DENEGAR ACCESO (no permitir por defecto)
- Registrar intentos de acceso no autorizado en auditoría

Validación:

```bash
# Debe retornar 401/403 sin token
curl -X POST http://localhost:8080/mcp/invoke \
  -H "Content-Type: application/json"
```

---

## 5. Configuración: Cero Hardcoding

✓ **Todas las URLs deben estar en `application.yaml`**

Ejemplo correcto:

```yaml
# application.yaml
external-apis:
  simpsons:
    base-url: ${SIMPSONS_API_BASE_URL:https://api.simpsonsquotable.com}
    timeout-ms: 5000
```

```java
// En el Adapter
@Value("${external-apis.simpsons.base-url}")
private String baseUrl;
```

---

## 6. Limpieza: Remover Placeholders

✓ **Eliminar TODAS las clases "Sample" o "Placeholder"**

Búsqueda de artefactos residuales:

```bash
# Encontrar clases Sample/Placeholder
find ./domain ./infrastructure ./applications -type f -name "*Sample*" -o -name "*Placeholder*"

# Encontrar hardcoding de URLs
grep -r 'http[s]\?://' ./domain ./infrastructure ./applications | grep -v 'application.yaml'

# Encontrar WebClient/RestTemplate fuera de Adapters
grep -r 'WebClient\|RestTemplate' ./domain ./applications
```

---

## 7. Validación de Convenciones del Workflow

Para cada Tool MCP generada, verificar:

- [ ] Existe archivo `api-contract-to-mcp.md` con matriz de mapeo
- [ ] Cada Tool está documentada con nombre, descripción y objetivo
- [ ] UseCase no contiene lógica de red (grep para WebClient, RestTemplate)
- [ ] Adapter implementa integración HTTP correctamente
- [ ] `application.yaml` contiene todas las URLs base necesarias
- [ ] Seguridad Entra ID está implementada y validada
- [ ] Clases Sample/Placeholder fueron removidas
- [ ] Tests existentes pasan: `./gradlew test`
- [ ] Se puede compilar sin errores: `./gradlew build`

---

## 8. Fases de Implementación Esperadas

### Fase 1: Intake (Análisis)

- Parsear contratos (Postman/OpenAPI)
- Generar matriz `api-contract-to-mcp.md` con todas las operaciones
- Clasificar cada operación como Tool / Resource / Prompt
- Identificar baseline técnico actual

### Fase 2: Scaffolding (Generación)

```bash
# Ejecutar en secuencia:
./gradlew.bat cleanArchitecture --package=co.com.bancolombia
./gradlew.bat generateEntryPoint --type=mcp
./gradlew.bat gm --name <ModelName>  # Para cada modelo de dominio
./gradlew.bat guc --name <UseCaseName>  # Para cada caso de uso
```

### Fase 3: Refinement (Implementación)

- Implementar modelos de dominio
- Implementar lógica de Use Cases
- Implementar Driven Adapters para integración HTTP
- Configurar seguridad Entra ID

### Fase 4: Security (Autorización)

- Aplicar `@PreAuthorize` en cada Tool
- Validar política fail-closed
- Implementar auditoría
- Escribir tests de seguridad

### Fase 5: Cleanup (Finalización)

- Remover todos los placeholders Sample
- Validar matriz de traceabilidad
- Ejecutar tests completos
- Generar reporte final

---

## 9. Reporte Final Requerido

Al completar, entregar un resumen estructurado:

```markdown
## Resumen de Tools MCP Implementadas

| Tool MCP | Ruta HTTP Original | UseCase Asociado | Adapter Usado | Rol Requerido | Estado |
|----------|-------------------|-----------------|---------------|---------------|--------|
| GetSimpsonsCharacter | GET /characters/{id} | GetSimpsonsCharacterUseCase | FetchSimpsonsAdapter | MCP.READ.CHARACTER | ✓ |
| CreateOrder | POST /orders | CreateOrderUseCase | OrderServiceAdapter | MCP.WRITE.ORDER | ✓ |

### Validaciones Ejecutadas
- ✓ Compilación: `./gradlew build` (0 errores)
- ✓ Tests: `./gradlew test` (X/X pasados)
- ✓ Seguridad: Todos los endpoints rechazan sin token
- ✓ Configuración: Cero URLs hardcodeadas
- ✓ Limpieza: 0 clases Sample/Placeholder encontradas
```

---

## Referencias

- 📘 [agent-api-to-mcp-workflow.md](../../docs/playbooks/agent-api-to-mcp-workflow.md) - LECTURA
  CRÍTICA
- 🏗️ [scaffold-baseline.md](../../docs/playbooks/scaffold-baseline.md)
- 🔒 [security-baseline.md](../../docs/playbooks/security-baseline.md)
- 🗺️ [api-contract-to-mcp.md](../../docs/templates/api-contract-to-mcp.md) - Template
- 🌐 [SDD Framework](https://github.com/kenyodarz/bancolombia-api-sdd-framework-export)
- 📋 [init-bancolombia-sdd.md](../../init-bancolombia-sdd.md) - Inyección del Framework

