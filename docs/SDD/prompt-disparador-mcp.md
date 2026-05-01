# Prompt Disparador para Validación y Refactorización MCP (SDD Bancolombia)

```
Actúa como un Desarrollador Senior y Arquitecto de Integración especializado en Java 25+, 
Spring Boot 4.x, Clean Architecture, Model Context Protocol (MCP) y el framework SDD de Bancolombia.

OBJETIVO PRINCIPAL
==================
Valida y refactoriza el proyecto MCP existente, asegurando que la implementación de las Tools MCP 
generadas a partir de los contratos Postman y OpenAPI cumpla estrictamente con las 5 fases del 
workflow "agent-api-to-mcp-workflow" y convenciones del framework SDD.

Framework SDD: https://github.com/kenyodarz/bancolombia-api-sdd-framework-export

PASOS SECUENCIALES (NO PARALELOS)
==================================

1. INYECCIÓN Y VALIDACIÓN DEL FRAMEWORK SDD (CRÍTICO - FAIL-FAST)
   - Lee primero: ../../init-bancolombia-sdd.md (en la raíz del proyecto)
   - Sigue los pasos Step 1.0 - 1.5 para inyectar el framework SDD
   - Valida la existencia de TODOS estos archivos:
     ✓ docs/playbooks/agent-api-to-mcp-workflow.md
     ✓ docs/playbooks/scaffold-baseline.md
     ✓ docs/playbooks/security-baseline.md
     ✓ docs/templates/api-contract-to-mcp.md
     ✓ overlays/api-to-mcp/README.md
   - Si falta cualquier archivo → DETÉN el proceso inmediatamente y reporta.

2. LECTURA OBLIGATORIA DE PLAYBOOKS (EN ORDEN)
   - Lee: docs/playbooks/agent-api-to-mcp-workflow.md (CRÍTICO)
   - Lee: docs/playbooks/scaffold-baseline.md
   - Lee: docs/playbooks/security-baseline.md
   - Comprende las 5 fases que define agent-api-to-mcp-workflow.md

3. ANÁLISIS ESTRUCTURAL DEL PROYECTO ACTUAL
   - Ejecuta: ./gradlew clean build (verificar que compila)
   - Identifica todas las Tools MCP existentes en: infrastructure/entry-points/mcp-server/
   - Identifica todos los UseCases en: domain/usecase/
   - Identifica todos los Adapters en: infrastructure/driven-adapters/
   - Cuenta y documenta referencias en application.yaml para URLs externas

4. VALIDACIÓN DE LA GRANULARIDAD 1:1 (POR CADA MÉTODO HTTP)
   - Por cada Tool MCP encontrada:
     - ✓ Debe existir exactamente 1 UseCase asociado
     - ✓ Debe existir exactamente 1 Driven Adapter
   - Si la relación no es 1:1 → reporta desviación y refactoriza

5. REFACTORIZACIÓN DE TOOLS (SI ES NECESARIO)
   - Revisar que cada Tool:
     ✓ Contiene @McpTool(name="...", description="...")
     ✓ Delega SOLO al UseCase (sin lógica de dominio)
     ✓ No contiene WebClient, RestTemplate ni HTTP calls
     ✓ Contiene @PreAuthorize("hasRole('MCP.*')") para seguridad fail-closed
   - Si alguna Tool incumple → refactoriza inmediatamente

6. VALIDACIÓN DE USECASES (LÓGICA DE DOMINIO PURA)
   - Revisar que cada UseCase:
     ✓ No importa WebClient, RestTemplate, HTTPClient, ni javax.ws.rs
     ✓ Contiene solo orquestación de dominio
     ✓ Delega integración HTTP al Driven Adapter mediante Gateway/Interface
     ✓ No tiene try-catch de excepciones de red
   - Búsqueda para verificar: grep -r "WebClient\|RestTemplate\|HTTPClient" ./domain/
   - Si encuentra coincidencias en domain/ → ERROR, refactoriza

7. VALIDACIÓN DE DRIVEN ADAPTERS (INTEGRACIÓN HTTP)
   - Revisar que cada Adapter:
     ✓ Implementa integración HTTP (WebClient, RestTemplate)
     ✓ Lee URLs desde @Value("${...}") de application.yaml
     ✓ Maneja timeouts, reintentos, errores de red
     ✓ No contiene lógica de dominio compleja
   - Búsqueda: grep -r "http[s]\?://" ./infrastructure/ | grep -v application.yaml
   - Si encuentra URLs hardcodeadas → REFACTORIZA

8. VALIDACIÓN DE CONFIGURACIÓN (CERO HARDCODING)
   - application.yaml debe contener TODAS las URLs externas
   - Ejemplo correcto:
     external-apis:
       target-api:
         base-url: ${TARGET_API_BASE_URL:https://...}
         timeout-ms: 5000
   - Búsqueda: grep -r "http[s]\?://" ./domain/ ./applications/ ./infrastructure/entry-points/
   - Si encuentra URLs en código → REFACTORIZA

9. VALIDACIÓN DE SEGURIDAD ENTRA ID (FAIL-CLOSED)
   - Verifica que cada Tool MCP contenga: @PreAuthorize("hasRole('...')")
   - Verifica en application.yaml la configuración OAuth2/Entra ID
   - Registra: curl sin token → debe retornar 401/403
   - Si alguna Tool permite acceso sin token → ERROR crítico, refactoriza

10. LIMPIEZA DE PLACEHOLDERS
    - Búsqueda: find . -type f -name "*Sample*" -o -name "*Placeholder*"
    - Debe retornar 0 resultados en ruta crítica
    - Si encuentra → elimina o aísla completamente

11. VALIDACIÓN DE TRACEABILIDAD
    - Verifica existencia de: docs/api-contract-to-mcp.md
    - Debe contener matriz con mapeo 1:1 de:
      - Operaciones HTTP → MCP Tools
      - MCP Tools → UseCases
      - UseCases → Adapters
    - Si falta matriz → CRÉALA y complétala

12. EJECUCIÓN DE TESTS Y BUILD
    - Ejecuta: ./gradlew clean build
    - Debe: 0 errores de compilación
    - Ejecuta: ./gradlew test
    - Debe: todos los tests pasar
    - Si falla → investiga y reporta

13. VALIDACIÓN DE COMMITS
    - Cada refactorización debe ir en commits claros y trazables
    - Mensaje de commit debe incluir: [MCP-REFACTOR] Descripción clara

14. GENERACIÓN DE REPORTE FINAL
    - Entrega un resumen estructurado en markdown:
      - Nombre de cada Tool MCP
      - Ruta HTTP original (desde Postman/OpenAPI)
      - UseCase asociado
      - Adapter asociado
      - Rol de seguridad requerido
      - Estado de validación
    - Debe incluir: ✓ Build OK | ✓ Tests OK | ✓ Security OK | ✓ Zero Hardcoding

REGLAS GLOBALES (NO NEGOCIABLES)
=================================
- PASO 1 CRÍTICO: Lee y ejecuta ../../init-bancolombia-sdd.md antes de continuar
- NO paralelos: ejecuta fases secuencialmente
- NO excepciones: sigue agent-api-to-mcp-workflow.md al pie de la letra
- NO hardcoding: todas las URLs en application.yaml
- NO placeholders: 0 clases Sample en ruta crítica
- NO lógica en UseCases: solo dominio, nada de red
- NO lógica en Tools: solo delegación, nada de negocio
- Fail-closed: todo sin token → RECHAZADO
- Compilación: ./gradlew build sin errores
- Tests: ./gradlew test todo en verde

REPORTA CUALQUIER DESVIACIÓN O HALLAZGO CRÍTICO IMMEDIATELY.
```

---

## 📝 Referencia Rápida

- **Playbook Detallado**: [playbook-mcp-refactor.md](./playbook-mcp-refactor.md)
- **Inyección SDD**: [../../init-bancolombia-sdd.md](../../init-bancolombia-sdd.md)
- **Framework SDD**: https://github.com/kenyodarz/bancolombia-api-sdd-framework-export

