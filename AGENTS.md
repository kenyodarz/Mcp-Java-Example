# AGENTS.md - Mcp-Java-Example (MCP Server)

This project is a reactive MCP (Model Context Protocol) Server built with Java, Spring AI, and Clean Architecture.

## Project Overview

- **Core Technology**: Java 17+, Spring Boot 3.x, Spring AI MCP.
- **Architecture**: Clean Architecture (Domain, UseCases, Infrastructure, Application modules).
- **Purpose**: Provide a set of tools and resources via the Model Context Protocol, specifically integrating with The Simpsons API.

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

## Development Workflow

1. Use `./gradlew build` to verify logic and tool definitions.
2. New tools should be added as Use Cases and then exposed through the MCP adapter.
3. Keep `catalog-info.yaml` updated to reflect the server's metadata for Backstage/MCP catalogs.

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

