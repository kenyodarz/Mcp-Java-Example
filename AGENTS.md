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
