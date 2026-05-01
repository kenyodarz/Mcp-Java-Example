# 📚 Wiki — Documentación Contextual del Proyecto MCP

Esta carpeta contiene documentación **complementaria y de referencia** del proyecto.  
No es necesaria para el día a día del desarrollo, pero es útil cuando necesitas profundizar en un
tema específico.

> **Para desarrolladores y agentes**: Empieza por el código fuente y `AGENTS.md`.  
> Consulta aquí solo si necesitas contexto adicional sobre un tema concreto.

---

## 📁 Estructura

```
wiki/
├── guides/           ← Guías de uso y seguridad
├── technical/        ← Documentación técnica de integración
└── techdocs/         ← Configuración y preview de TechDocs
```

---

## 📖 Contenido por Carpeta

### 🧭 `guides/` — Guías de Uso y Seguridad

Documentación orientada a desarrolladores y operadores del servidor MCP.

| Archivo                                                                             | Descripción                                                 |
|-------------------------------------------------------------------------------------|-------------------------------------------------------------|
| [MCP_USER_GUIDE.md](guides/MCP_USER_GUIDE.md)                                       | Guía de uso del servidor MCP y sus herramientas             |
| [SECURITY_GUIDE.md](guides/SECURITY_GUIDE.md)                                       | Guía general de seguridad del proyecto                      |
| [IMPLEMENTACION_SEGURIDAD_ENTRA_ID.md](guides/IMPLEMENTACION_SEGURIDAD_ENTRA_ID.md) | Implementación detallada de seguridad con Entra ID (OAuth2) |

### 🔧 `technical/` — Documentación Técnica

Referencia técnica de integraciones y arquitectura del sistema.

| Archivo                                                            | Descripción                                      |
|--------------------------------------------------------------------|--------------------------------------------------|
| [SPRING_AI_INTEGRATION.md](technical/SPRING_AI_INTEGRATION.md)     | Integración detallada con Spring AI y módulo MCP |
| [Estructura de Módulos.md](technical/Estructura%20de%20Módulos.md) | Descripción de los módulos Gradle del proyecto   |

### 📄 `techdocs/` — TechDocs (Backstage)

Configuración y guías para el sistema de documentación TechDocs de Backstage.

| Archivo                                                         | Descripción                          |
|-----------------------------------------------------------------|--------------------------------------|
| [techdocs.md](techdocs/techdocs.md)                             | Documentación general de TechDocs    |
| [TECHDOCS_PREVIEW_GUIDE.md](techdocs/TECHDOCS_PREVIEW_GUIDE.md) | Cómo hacer preview local de TechDocs |

---

## 🎯 ¿Cuándo consultar esta carpeta?

| Situación                                    | Archivo a consultar                           |
|----------------------------------------------|-----------------------------------------------|
| Quiero entender cómo usar el servidor MCP    | `guides/MCP_USER_GUIDE.md`                    |
| Necesito configurar seguridad con Entra ID   | `guides/IMPLEMENTACION_SEGURIDAD_ENTRA_ID.md` |
| Quiero entender la integración con Spring AI | `technical/SPRING_AI_INTEGRATION.md`          |
| Quiero ver la estructura de módulos          | `technical/Estructura de Módulos.md`          |
| Quiero hacer preview de docs en Backstage    | `techdocs/TECHDOCS_PREVIEW_GUIDE.md`          |

---

## 🔗 Documentación Principal del Proyecto

La documentación **operativa y de desarrollo** vive en:

- [`docs/SDD/`](../docs/SDD/) — Framework SDD: playbook, prompt disparador
- [`docs/setup/`](../docs/setup/) — Inyección del framework SDD
- [`docs/guides/`](../docs/guides/) — Guías de inicio rápido
- [`docs/playbooks/`](../docs/playbooks/) — Playbooks inyectados del SDD
- [`docs/templates/`](../docs/templates/) — Templates del SDD

---

**Repositorio SDD**: https://github.com/kenyodarz/bancolombia-api-sdd-framework-export

