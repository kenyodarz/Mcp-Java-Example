# Especificación: Secrets Manager Adapter (Ministack + AWS Secrets Manager)

## Objetivo

Abstraer la gestión de secretos para consumo de APIs externas y configuración MCP en ambientes:

- **desarrollo local**: Ministack
- **producción**: AWS Secrets Manager

## Secretos requeridos

| Nombre Secret       | Propósito                       | Scope         | Almacenamiento Dev | Almacenamiento Prod |
|---------------------|---------------------------------|---------------|--------------------|---------------------|
| `api-consumer-key`  | Key para consumir APIs externas | REST Consumer | Ministack          | AWS Secrets Manager |
| `mcp-client-id`     | Client ID para MCP              | MCP Server    | Ministack          | AWS Secrets Manager |
| `mcp-client-secret` | Secret para MCP                 | MCP Server    | Ministack          | AWS Secrets Manager |

## Arquitectura

```text
MCP Tool / REST Consumer UseCase
                ↓
        AsyncSecretsGateway
                ↓
        SecretsManagerAdapter
                ↓
        SecretsManagerAsyncClient
                ↓
 [Ministack (dev) | AWS Secrets Manager (prod)]
```

## Decisión técnica clave

La selección entre entorno local y AWS real **no** se hace con una propiedad de nombre heredado.

Se define así:

- si `aws.secretsmanager.endpoint` tiene valor, se usa endpoint override hacia Ministack
- si `aws.secretsmanager.endpoint` está vacío, se usa AWS real

## Configuration Properties

### Base

```yaml
aws:
  region: "${AWS_REGION:us-east-1}"
  secretsmanager:
    endpoint: "${AWS_SECRETS_ENDPOINT:}"
    cache-size: 5
    cache-seconds: 3600
```

### Desarrollo

```yaml
aws:
  region: us-east-1
  secretsmanager:
    endpoint: http://localhost:4566
    cache-size: 5
    cache-seconds: 3600
```

### Producción

```yaml
aws:
  region: "${AWS_REGION}"
  secretsmanager:
    endpoint: ""
    cache-size: 10
    cache-seconds: 7200
```

## Gateway

```java
public interface AsyncSecretsGateway {

    Mono<String> getSecret(String secretName);
}
```

## Adapter

`SecretsManagerAdapter` implementa `AsyncSecretsGateway` usando `SecretsManagerAsyncClient`.

## Use cases

- `GetApiSecretUseCase`
- `GetMcpSecretsUseCase`

## Validación esperada

- compilación verde
- tests verdes
- configuración sin referencias residuales a LocalStack
- documentación alineada con Ministack
