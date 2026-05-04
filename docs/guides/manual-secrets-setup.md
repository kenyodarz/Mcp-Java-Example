# Configuración Manual de Secretos en Ministack

> **Nota**: Este documento proporciona comandos manuales para crear secretos en AWS Secrets Manager
> via Ministack. La automatización shell anterior (`init-secrets.sh`) ha sido deprecada debido a
> fallos silenciosos.

## Requisitos previos

- **Docker** y **Docker Compose** instalados
- **AWS CLI** instalado localmente
- **Ministack** iniciado (`docker-compose up -d`)

## Verificar que Ministack está corriendo

```bash
# Ver estado de los contenedores
docker compose ps

# Verificar health del endpoint
curl http://localhost:4566/_ministack/health
```

**Resultado esperado**: `{"status": "ok"}`

---

## Opción 1: Comandos AWS CLI (Recomendado)

Configura las variables de entorno y ejecuta los comandos directamente:

```bash
# Configurar variables de entorno
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export AWS_ENDPOINT=http://localhost:4566

# Crear secreto: api-consumer-key
aws secretsmanager create-secret \
    --name api-consumer-key \
    --secret-string "simpsons-api-key-dev-12345" \
    --description "API Consumer Key para integración con APIs externas" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region us-east-1

# Crear secreto: mcp-client-id
aws secretsmanager create-secret \
    --name mcp-client-id \
    --secret-string "mcp-dev-client-id-abc123" \
    --description "Client ID para autenticación MCP" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region us-east-1

# Crear secreto: mcp-client-secret
aws secretsmanager create-secret \
    --name mcp-client-secret \
    --secret-string "mcp-dev-client-secret-xyz789-super-secret" \
    --description "Client Secret para autenticación MCP" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region us-east-1
```

---

## Opción 2: Comandos via Docker Compose

Si el AWS CLI no está instalado localmente, ejecuta los comandos dentro del contenedor Ministack:

```bash
# Configurar variables de entorno
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export AWS_ENDPOINT=http://localhost:4566

# Crear secreto: api-consumer-key
docker compose exec -T ministack aws secretsmanager create-secret \
    --name api-consumer-key \
    --secret-string "simpsons-api-key-dev-12345" \
    --description "API Consumer Key para integración con APIs externas" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region us-east-1

# Crear secreto: mcp-client-id
docker compose exec -T ministack aws secretsmanager create-secret \
    --name mcp-client-id \
    --secret-string "mcp-dev-client-id-abc123" \
    --description "Client ID para autenticación MCP" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region us-east-1

# Crear secreto: mcp-client-secret
docker compose exec -T ministack aws secretsmanager create-secret \
    --name mcp-client-secret \
    --secret-string "mcp-dev-client-secret-xyz789-super-secret" \
    --description "Client Secret para autenticación MCP" \
    --endpoint-url "$AWS_ENDPOINT" \
    --region us-east-1
```

**Nota**: El flag `-T` deshabilita la asignación de pseudo-terminal, necesario para CI/CD.

---

## Verificar que los secretos fueron creados

### Listar todos los secretos

```bash
aws secretsmanager list-secrets \
    --query 'SecretList[*].[Name,Description]' \
    --output table \
    --endpoint-url "http://localhost:4566" \
    --region us-east-1
```

**Resultado esperado**:

```
-------------------------------------------------------
|                    SecretList                       |
+------------------+--------------------------------+
| api-consumer-key | API Consumer Key para...       |
| mcp-client-id    | Client ID para autenticación.. |
| mcp-client-secret| Client Secret para autent...   |
+------------------+--------------------------------+
```

### Obtener el valor de un secreto específico

```bash
aws secretsmanager get-secret-value \
    --secret-id api-consumer-key \
    --query 'SecretString' \
    --output text \
    --endpoint-url "http://localhost:4566" \
    --region us-east-1
```

**Resultado esperado**: `simpsons-api-key-dev-12345`

---

## Operaciones adicionales

### Actualizar el valor de un secreto

```bash
aws secretsmanager update-secret \
    --secret-id api-consumer-key \
    --secret-string "nuevo-valor-secreto" \
    --endpoint-url "http://localhost:4566" \
    --region us-east-1
```

### Eliminar un secreto

```bash
aws secretsmanager delete-secret \
    --secret-id api-consumer-key \
    --force-delete-without-recovery \
    --endpoint-url "http://localhost:4566" \
    --region us-east-1
```

**Nota**: Use `--force-delete-without-recovery` para eliminar inmediatamente. Sin este flag, el
secreto entra en período de recuperación de 7 días.

### Recuperar secreto eliminado (durante período de recuperación)

```bash
aws secretsmanager restore-secret \
    --secret-id api-consumer-key \
    --endpoint-url "http://localhost:4566" \
    --region us-east-1
```

---

## Script de utilidades (alternativa)

También puede usar el script `scripts/ministack-tools.sh` para ejecutar estos comandos de forma más
fácil:

```bash
# Crear secretos
./scripts/ministack-tools.sh create-secrets

# Listar secretos
./scripts/ministack-tools.sh list-secrets

# Obtener valor de un secreto
./scripts/ministack-tools.sh get-secret api-consumer-key

# Eliminar un secreto
./scripts/ministack-tools.sh delete-secret api-consumer-key

# Setup completo (inicia Ministack + crea secretos)
./scripts/ministack-tools.sh dev-setup
```

---

## Solución de problemas

### Problema: "The Secrets Manager operation failed because the client was unable to connect"

**Causa**: Ministack no está corriendo o el endpoint es incorrecto.

**Solución**:

```bash
# Verificar que Ministack está corriendo
docker compose ps

# Iniciar Ministack si está detenido
docker compose up -d ministack

# Esperar 15 segundos para que el health check pase
sleep 15

# Verificar health
curl http://localhost:4566/_ministack/health
```

### Problema: "An error occurred (ResourceExistsException) when calling the CreateSecret operation"

**Causa**: El secreto ya existe.

**Solución**: Actualizar el secreto existente:

```bash
aws secretsmanager update-secret \
    --secret-id api-consumer-key \
    --secret-string "nuevo-valor" \
    --endpoint-url "http://localhost:4566" \
    --region us-east-1
```

O eliminarlo primero:

```bash
aws secretsmanager delete-secret \
    --secret-id api-consumer-key \
    --force-delete-without-recovery \
    --endpoint-url "http://localhost:4566" \
    --region us-east-1
```

### Problema: "command not found: aws"

**Causa**: AWS CLI no está instalado.

**Soluciones**:

1. Instalar AWS CLI: https://aws.amazon.com/cli/
2. Usar la opción "Comandos via Docker Compose" (Opción 2)

---

## Resumen de valores de secretos

| Secreto             | Descripción                                         | Valor                                       |
|---------------------|-----------------------------------------------------|---------------------------------------------|
| `api-consumer-key`  | API Consumer Key para integración con APIs externas | `simpsons-api-key-dev-12345`                |
| `mcp-client-id`     | Client ID para autenticación MCP                    | `mcp-dev-client-id-abc123`                  |
| `mcp-client-secret` | Client Secret para autenticación MCP                | `mcp-dev-client-secret-xyz789-super-secret` |

---

## Próximos pasos

1. **Ejecutar los comandos anteriores** para crear los secretos
2. **Verificar** que los secretos se crearon exitosamente
3. **Iniciar la aplicación**: `./gradlew :app-service:bootRun`
4. **Confirmar** que la aplicación puede acceder a los secretos

---

**Última actualización**: 2025-05-04  
**Referencia**: Spec-Driven Development (SDD) - Bancolombia Clean Architecture

