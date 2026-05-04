# Traceability Matrix: Secrets Manager Adapter (ministack + AWS)

## Specification Compliance

| Artifact                   | Specification Reference | Implementation                                                                                                | Status |
|----------------------------|-------------------------|---------------------------------------------------------------------------------------------------------------|--------|
| **Gateway Interface**      | `AsyncSecretsGateway`   | `domain/model/src/main/java/.../model/gateway/AsyncSecretsGateway.java`                                       | ✅      |
| **Adapter Implementation** | `SecretsManagerAdapter` | `infrastructure/driven-adapters/secrets-manager-adapter/src/main/java/.../adapter/SecretsManagerAdapter.java` | ✅      |
| **Configuration Bean**     | `SecretsConfig`         | `applications/app-service/src/main/java/.../config/SecretsConfig.java`                                        | ✅      |
| **UseCase: API Secret**    | `GetApiSecretUseCase`   | `domain/usecase/src/main/java/.../usecase/GetApiSecretUseCase.java`                                           | ✅      |
| **UseCase: MCP Secret**    | `GetMcpSecretsUseCase`  | `domain/usecase/src/main/java/.../usecase/GetMcpSecretsUseCase.java`                                          | ✅      |

## Configuration Files

| File                    | Scope                        | Purpose                   | Status |
|-------------------------|------------------------------|---------------------------|--------|
| `application.yaml`      | Base (común)                 | Propiedades base AWS      | ✅      |
| `application-dev.yaml`  | Development (ministack:4566) | Overrides para desarrollo | ✅      |
| `application-prod.yaml` | Production (AWS real)        | Overrides para producción | ✅      |

## Module Registration

| Module                      | Path                                                      | Gradle Registration                     | Status |
|-----------------------------|-----------------------------------------------------------|-----------------------------------------|--------|
| **secrets-manager-adapter** | `infrastructure/driven-adapters/secrets-manager-adapter/` | `:secrets-manager` en `settings.gradle` | ✅      |
| **Dependency: app-service** | `applications/app-service/`                               | Agregada en `build.gradle`              | ✅      |

## Test Coverage

| Class                   | Test File                        | Tests | Status |
|-------------------------|----------------------------------|-------|--------|
| `SecretsManagerAdapter` | `SecretsManagerAdapterTest.java` | 3     | ✅      |
| `GetApiSecretUseCase`   | `GetApiSecretUseCaseTest.java`   | 3     | ✅      |
| `GetMcpSecretsUseCase`  | `GetMcpSecretsUseCaseTest.java`  | 3     | ✅      |

---

## Secrets Mapping

| Secret Name         | Purpose                     | Dev (ministack) | Prod (AWS)          | Created By   |
|---------------------|-----------------------------|-----------------|---------------------|--------------|
| `api-consumer-key`  | API Consumer Authentication | ministack:4566  | AWS Secrets Manager | Admin/DevOps |
| `mcp-client-id`     | MCP Client ID               | ministack:4566  | AWS Secrets Manager | Admin/DevOps |
| `mcp-client-secret` | MCP Client Secret           | ministack:4566  | AWS Secrets Manager | Admin/DevOps |

---

## Architecture Compliance

### Clean Architecture Layers

✅ **Domain Layer (model)**

- `AsyncSecretsGateway` - Pure interface, no infrastructure concerns

✅ **Domain Layer (usecase)**

- `GetApiSecretUseCase` - No network code, orchestration only
- `GetMcpSecretsUseCase` - No network code, aggregation logic

✅ **Infrastructure Layer (driven-adapter)**

- `SecretsManagerAdapter` - HTTP client implementation via AWS SDK
- Reactive types: `Mono<String>`, `Mono<Map<String, String>>`

✅ **Application Layer (app-service)**

- `SecretsConfig` - Bean factory and wiring
- Profile-based configuration: dev vs prod

### Non-Functional Requirements

| Requirement                | Implementation                                    | Status |
|----------------------------|---------------------------------------------------|--------|
| **Reactivity**             | All types return `Mono<T>`                        | ✅      |
| **Caching**                | Delegated to AWS SDK (5 items dev, 10 items prod) | ✅      |
| **Security (Fail-Closed)** | Errors propagated, no defaults                    | ✅      |
| **Observability**          | @Slf4j logging at DEBUG level                     | ✅      |
| **Timeout**                | AWS SDK + Resilience4j ready                      | ✅      |
| **Environment Adaptation** | Spring profiles: dev, prod                        | ✅      |

---

## Build and Test Validation

```bash
# Build the entire project
./gradlew build

# Run all tests (including secrets-manager-adapter tests)
./gradlew test

# Run only secrets-manager-adapter tests
./gradlew :secrets-manager:test

# Run only usecase tests
./gradlew :usecase:test
```

---

## Deployment Checklist

- [ ] secrets-manager-adapter module builds successfully
- [ ] All unit tests pass
- [ ] `./gradlew build` completes without errors
- [ ] `application-dev.yaml` points to ministack:4566
- [ ] `application-prod.yaml` uses AWS Secrets Manager
- [ ] AWS SDK dependencies are minimal and reactive-compatible
- [ ] SecretsConfig bean is properly scanned by Spring
- [ ] `@Service` annotations on UseCases are auto-wired
- [ ] No hardcoded secrets in code or yaml
- [ ] Documentation updated with secret names

---

## Integration Points (Next Phase)

These components are ready to be consumed by:

1. **REST Consumer Adapter** - Inyectar `GetApiSecretUseCase` para obtener API keys
2. **MCP Server Tools** - Inyectar `GetMcpSecretsUseCase` para validar requests
3. **Any other service** - Inyectar `AsyncSecretsGateway` directamente

---

**Status**: Phase 3 Refinement Complete. Ready for Phase 4: Security & Integration.

**Last Updated**: 2026-05-04

