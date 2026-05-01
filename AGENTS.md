# AGENTS.md - Mcp-Java-Example (MCP Server)

This project is a reactive MCP (Model Context Protocol) Server built with Java, Spring AI, and Clean Architecture.

## Project Overview

- **Core Technology**: Java 25, Spring Boot 4.x, Spring AI 2.0.0 M4, Jackson 3, Gradle 9.5.0.
- **Scaffolder**: Bancolombia Clean Architecture Scaffolder v4.4.1.
- **Architecture**: Clean Architecture (Domain, UseCases, Infrastructure, Application modules).
- **Purpose**: Provide a set of tools and resources via the Model Context Protocol, specifically integrating with The Simpsons API.

## SDD Operating Goals

This repository follows **Spec-Driven Development (SDD)** on top of the **Bancolombia Clean
Architecture scaffold**. The operating goal is:

1. Understand the current baseline
2. Define or refine the specification
3. Map the change to architecture
4. Generate structure when appropriate
5. Implement, validate, and document with traceability

### Core Rule

**Do not implement first. Fix the spec first.**

*Controlled exception*: if the required delivery baseline is still missing, structural scaffold
bootstrap may happen before a concrete feature is implemented.

## Reading Order (Before Changing Code)

1. `docs/playbooks/scaffold-baseline.md`
2. `docs/playbooks/security-baseline.md` (when the repository exposes protected capabilities)
3. `docs/sdd/README.md`
4. `docs/templates/`
5. The current initiative folder
6. The applicable `skills/`
7. The relevant overlay if the repo adopts API → MCP or a security baseline

## Setup and Build Commands

- **Build**: `./gradlew build`
- **Clean and Build**: `./gradlew clean build`
- **Run Locally**: `./gradlew :app-service:bootRun`
- **Configuration**: Main config is in `applications/app-service/src/main/resources/application.yaml`.

## Testing Instructions

- **Run all tests**: `./gradlew test`
- **Run specific module tests**: `./gradlew :<module-name>:test`

## Code Style and Conventions

- **MCP Tools**: Tools are implemented in the `infrastructure` layer and exposed as MCP tool callbacks.
- **Clean Architecture**: 
  - `domain/model`: Domain logic and data structures.
  - `domain/usecase`: Business scenarios.
  - `infrastructure/driven-adapters`: External integrations (e.g., Simpsons API client).
  - `infrastructure/entry-points`: MCP Server stream endpoints.
- **Reactive Stack**: All logic must be non-blocking using `Mono` and `Flux`.
- **Security**: Supports OAuth2/Entra ID for tool execution protection.

## Architecture Invariants (Non-Negotiable)

- Keep business logic out of controllers, tools, prompts, resources, and adapters.
- Keep the domain free of infrastructure-specific concerns.
- Treat placeholders as scaffolding, not as business implementation.
- Do not assume default wiring is always sufficient for gateway-backed use cases.
- Do not let documented capabilities diverge from runtime behavior.

## Mandatory Development Workflow

1. Verify the current scaffold baseline
2. Work from a spec (read `docs/playbooks/agent-api-to-mcp-workflow.md`)
3. Map the change to Clean Architecture
4. Prefer scaffold generation for structural steps
5. Refine generated output into business code
6. Validate security, audit, resilience, and configuration where applicable
7. Maintain traceability between spec, code, config, and tests

## Minimum Definition of Done

A task is only complete if:

- [ ] A spec or spec update exists
- [ ] The technical decision is explicit
- [ ] Traceability exists between spec and code
- [ ] Generated structure respects the scaffold or documented baseline
- [ ] Relevant tests were executed
- [ ] Navigable documentation was updated
- [ ] Build passes: `./gradlew build`
- [ ] All tests pass: `./gradlew test`

## Repository Structure

```
mcp/
├── AGENTS.md                        ← You are here
├── README.md                        ← Project overview
│
├── docs/                            ← Operational documentation
│   ├── SDD/                         ← SDD Framework (playbook, prompt)
│   ├── setup/                       ← SDD injection instructions
│   ├── guides/                      ← Quick start guides
│   ├── playbooks/                   ← Injected from SDD Framework
│   └── templates/                   ← Injected from SDD Framework
│
├── wiki/                            ← Contextual/reference documentation
│   ├── guides/                      ← MCP usage, security guides
│   ├── technical/                   ← Spring AI, module structure
│   └── techdocs/                    ← Backstage TechDocs
│
├── domain/model/                    ← Domain entities and interfaces
├── domain/usecase/                  ← Business use cases
├── infrastructure/entry-points/     ← MCP Server tools (entry point)
├── infrastructure/driven-adapters/  ← External API integrations
└── applications/app-service/        ← Spring Boot application
```

## SDD Framework (Important for Agents)

This project follows the **Bancolombia SDD Framework**.

- **SDD Repository**: https://github.com/kenyodarz/bancolombia-api-sdd-framework-export
- **Workflow to follow**: `docs/playbooks/agent-api-to-mcp-workflow.md` (5 phases)
- **For new MCP tools**: Read `docs/SDD/playbook-mcp-refactor.md`
- **For automation**: Use `docs/SDD/prompt-disparador-mcp.md`

### Key Rules (Non-Negotiable)

- 1 HTTP Method = 1 MCP Tool + 1 UseCase + 1 Adapter
- No network code in UseCases (only in Driven Adapters)
- No business logic in MCP Tools (only delegation to UseCase)
- All external URLs must be in `application.yaml` (no hardcoding)
- Security: Fail-closed with `@PreAuthorize("hasRole('MCP.*')")`

