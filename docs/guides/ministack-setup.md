# 🚀 Guía: Ministack + Secrets Manager para desarrollo local

## Overview

Esta guía usa **Ministack** como emulador local de AWS para el desarrollo del proyecto.

Referencias oficiales verificadas:

- `https://ministack.org/`
- `https://github.com/ministackorg/ministack`
- DeepWiki: `deepwiki.com/ministackorg/ministack`

Puntos confirmados desde la documentación:

- imagen Docker oficial: `ministackorg/ministack:latest`
- health endpoint: `/_ministack/health`
- reset endpoint: `/_ministack/reset?init=1`
- init scripts soportados en `/docker-entrypoint-initaws.d/` y en fases `boot.d` / `ready.d`
- persistencia con `PERSIST_STATE=1` y `STATE_DIR=/tmp/ministack`
- wrapper `awslocal` existe, pero en este repo usamos `aws --endpoint-url ...` para evitar
  dependencias implícitas

## Secretos cargados en desarrollo

| Nombre              | Valor dev                                   | Uso                      |
|---------------------|---------------------------------------------|--------------------------|
| `api-consumer-key`  | `simpsons-api-key-dev-12345`                | consumo de APIs externas |
| `mcp-client-id`     | `mcp-dev-client-id-abc123`                  | client id del MCP        |
| `mcp-client-secret` | `mcp-dev-client-secret-xyz789-super-secret` | client secret del MCP    |

## Archivos involucrados

```text
/docker-compose.yml
/docker/init-secrets.sh
/scripts/ministack-tools.sh
/applications/app-service/src/main/resources/application.yaml
/applications/app-service/src/main/resources/application-dev.yaml
/applications/app-service/src/main/resources/application-prod.yaml
```

## Quick start

```bash
docker compose up -d
curl http://localhost:4566/_ministack/health
./scripts/ministack-tools.sh list-secrets
./gradlew :app-service:bootRun
```

## Comandos útiles

```bash
./scripts/ministack-tools.sh start
./scripts/ministack-tools.sh status
./scripts/ministack-tools.sh list-secrets
./scripts/ministack-tools.sh get-secret api-consumer-key
./scripts/ministack-tools.sh reset
./scripts/ministack-tools.sh stop
```

## Configuración usada por la aplicación

La aplicación no usa una bandera con nombre heredado; ahora decide el override del cliente AWS así:

- si `aws.secretsmanager.endpoint` tiene valor, el SDK apunta a Ministack
- si `aws.secretsmanager.endpoint` está vacío, usa AWS real

Ejemplo dev:

```yaml
aws:
  region: us-east-1
  secretsmanager:
    endpoint: http://localhost:4566
```

Ejemplo prod:

```yaml
aws:
  region: ${AWS_REGION}
  secretsmanager:
    endpoint: ""
```

## Persistencia

Ministack guarda estado local cuando:

```yaml
environment:
  - PERSIST_STATE=1
  - STATE_DIR=/tmp/ministack
volumes:
  - ministack-data:/tmp/ministack
```

## Inicialización automática

El repo monta `docker/init-secrets.sh` en:

```text
/docker-entrypoint-initaws.d/init-secrets.sh
```

Ese path está soportado por Ministack por compatibilidad. El script crea los secretos al arrancar el
contenedor.

## Troubleshooting

### El health endpoint no responde

```bash
docker compose logs ministack
curl http://localhost:4566/_ministack/health
```

### Los secretos no aparecen

```bash
./scripts/ministack-tools.sh create-secrets
./scripts/ministack-tools.sh list-secrets
```

### Quiero re-ejecutar los scripts de inicialización

```bash
./scripts/ministack-tools.sh reset
```

## Nota importante

Aunque Ministack es compatible con varias convenciones históricas, en este proyecto evitamos
mantener nomenclatura heredada en código, propiedades y documentación para que el baseline quede
explícitamente alineado con **Ministack**.

````
