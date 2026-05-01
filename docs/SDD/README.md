# 📁 Carpeta SDD: Framework Bancolombia SDD para MCP

Esta carpeta contiene toda la documentación y guías para validación, refactorización y mejora de
proyectos MCP basados en el **Framework SDD de Bancolombia**.

**Framework Official**: https://github.com/kenyodarz/bancolombia-api-sdd-framework-export

---

## 📄 Archivos en Esta Carpeta

### 1. **`playbook-mcp-refactor.md`** (GUÍA COMPLETA)

- **Propósito**: Instrucciones detalladas para validar y refactorizar un MCP ya implementado
- **Audiencia**: Desarrolladores que trabajan manualmente
- **Contenido**:
    - 9 secciones estructuradas
    - Comandos grep/find listos para copiar-pegar
    - Ejemplos de código correcto/incorrecto
    - Matriz de verificación paso a paso
    - 5 Fases del workflow SDD integradas

**Cuándo usarlo**: Cuando necesitas una guía paso a paso para validar arquitectura MCP

### 2. **`prompt-disparador-mcp.md`** (PARA AGENTES/LLMs)

- **Propósito**: Prompt directo y ejecutable para automatización con agentes IA
- **Audiencia**: Agentes, LLMs, pipelines de automatización
- **Contenido**:
    - 14 pasos secuenciales
    - Validaciones automáticas integradas
    - Referencias claras al archivo `init-bancolombia-sdd.md`
    - Reporte final estructurado en markdown
    - Reglas globales no negociables

**Cuándo usarlo**: Cuando quieres automatizar la validación/refactorización

---

## 🚀 Inicio Rápido

### Opción A: Validación Manual (Paso a Paso)

```bash
# 1. Lee la guía principal
cat playbook-mcp-refactor.md

# 2. Sigue los comandos de validación
grep -r 'http[s]\?://' ./domain ./infrastructure | grep -v 'application.yaml'
find . -type f -name "*Sample*" -o -name "*Placeholder*"
./gradlew clean build && ./gradlew test
```

### Opción B: Automatización con LLM/Agente

```bash
# 1. Copiar el prompt disparador
cat prompt-disparador-mcp.md | pbcopy  # macOS
# o
Get-Content prompt-disparador-mcp.md | Set-Clipboard  # PowerShell Windows

# 2. Pegar en tu LLM (ChatGPT, Claude, etc.)
# El LLM ejecutará 14 pasos automáticamente
```

---

## 🔑 Paso Crítico: Inyección del Framework SDD

**ANTES de usar estos archivos**, debes inyectar el framework SDD:

```bash
# En la raíz del proyecto:
cat ../../init-bancolombia-sdd.md

# Sigue los pasos Step 1.0 - 1.5
# El archivo tiene instrucciones detalladas y fail-fast
```

Este paso es **NO NEGOCIABLE**. Si falta, los playbooks no funcionarán correctamente.

---

## 📋 Estructura de Referencia

### Donde Encontrar Cada Cosa

```
Raíz del Proyecto
├── init-bancolombia-sdd.md              ← INYECCIÓN DEL FRAMEWORK SDD
├── MEJORAS-DOCUMENTACION.md             ← Documentación de cambios
└── docs/
    ├── playbooks/                       ← Inyectados desde SDD Framework
    │   ├── agent-api-to-mcp-workflow.md
    │   ├── scaffold-baseline.md
    │   └── security-baseline.md
    ├── templates/                       ← Inyectados desde SDD Framework
    │   └── api-contract-to-mcp.md
    └── SDD/                             ← ESTA CARPETA
        ├── README.md                    ← (Este archivo)
        ├── playbook-mcp-refactor.md     ← Guía de validación
        └── prompt-disparador-mcp.md     ← Para agentes/LLMs
```

---

## ✅ Checklist: ¿Está Todo Listo?

- [ ] Framework SDD inyectado (todos los archivos en docs/playbooks/)
- [ ] Archivo `init-bancolombia-sdd.md` presente en raíz
- [ ] Esta carpeta `docs/SDD/` con sus 2 archivos
- [ ] Has leído `init-bancolombia-sdd.md` y entendido los 5 Steps
- [ ] Has leído `playbook-mcp-refactor.md` o tienes a mano
- [ ] Estás listo para validar/refactorizar tu MCP

---

## 🎯 Las 5 Fases del Workflow SDD

Estos archivos están basados en las 5 fases del workflow SDD oficial:

1. **Intake and Traceability** (Diagnosis)
2. **Scaffolding and Bootstrap** (Generación)
3. **Clean Architecture Refinement** (Iterativo)
4. **MCP Integration and Security** (Autorización)
5. **Clean Up and Approval** (Definition of Done)

Ver: `docs/playbooks/agent-api-to-mcp-workflow.md` (si lo tienes inyectado)

---

## 🔗 Referencias Externas

- **Framework SDD**: https://github.com/kenyodarz/bancolombia-api-sdd-framework-export
- **Gist Oficial**: https://gist.github.com/kenyodarz/e100df94e7567910c7dfc64b8381016d
- **Clean Architecture**: Bancolombia Scaffold (Java 17+, Spring Boot 3.x)

---

## 📞 ¿Preguntas?

**P: ¿Por dónde empiezo realmente?**  
R: Lee `../../init-bancolombia-sdd.md` primero. Luego elige:

- Opción A si validarás manualmente
- Opción B si usarás un LLM/Agente

**P: ¿Qué pasa si me falta algún archivo?**  
R: Ejecuta `../../init-bancolombia-sdd.md` - te dirá exactamente qué falta

**P: ¿Puedo compartir estos archivos?**  
R: Sí, perfectamente. Son públicos y reutilizables

**P: ¿Funcionan con versiones antiguas de Spring Boot?**  
R: Estos playbooks asumen Spring Boot 3.x+. Ajusta según tu versión

---

## 📊 Estadísticas

- **Archivos en esta carpeta**: 2 (playbook + prompt)
- **Líneas de playbook**: ~216 líneas
- **Pasos en prompt**: 14 pasos secuenciales
- **Fases del workflow**: 5 fases (Intake → Cleanup)
- **Validaciones automáticas**: 9+ búsquedas grep/find incluidas

---

**Última actualización**: 2026-05-01  
**Status**: ✅ **LISTO PARA USAR**  
**Versión SDD**: Basado en https://github.com/kenyodarz/bancolombia-api-sdd-framework-export

