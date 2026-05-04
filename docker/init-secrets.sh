#!/bin/bash

# ============================================
# Script de Inicialización de Secretos - Ministack
# https://ministack.org/ | https://github.com/ministackorg/ministack
# ============================================
# Este script se ejecuta automáticamente cuando Ministack se inicia
# y crea los secretos necesarios para la aplicación MCP.

set -e

echo "=========================================="
echo " Inicializando Secretos en Ministack"
echo "=========================================="

# Configuración AWS CLI para Ministack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export AWS_ENDPOINT_URL_SECRETSMANAGER=http://localhost:4566

# Esperar a que Ministack esté listo
MINISTACK_RETRIES=0
MINISTACK_MAX_RETRIES=30

echo "⏳ Esperando a que Ministack esté listo..."
while [ $MINISTACK_RETRIES -lt $MINISTACK_MAX_RETRIES ]; do
    if curl -s http://localhost:4566/_ministack/health >/dev/null 2>&1; then
        echo "✅ Ministack está listo"
        break
    fi
    MINISTACK_RETRIES=$((MINISTACK_RETRIES + 1))
    echo "   Intento $MINISTACK_RETRIES/$MINISTACK_MAX_RETRIES..."
    sleep 2
done

if [ $MINISTACK_RETRIES -eq $MINISTACK_MAX_RETRIES ]; then
    echo "❌ Error: Ministack no respondió en tiempo"
    exit 1
fi

# ============================================
# Crear Secretos usando AWS CLI
# ============================================

echo ""
echo "📝 Creando secretos en AWS Secrets Manager emulado por Ministack..."

# Secret 1: API Consumer Key
echo "  → Creando secret: api-consumer-key"
aws secretsmanager create-secret \
    --name api-consumer-key \
    --secret-string "simpsons-api-key-dev-12345" \
    --description "API Consumer Key para integración con APIs externas (Simpsons)" \
    --tags Key=Environment,Value=Development Key=Application,Value=mcp-bancolombia \
    --endpoint-url http://localhost:4566 \
    --region us-east-1 \
    2>/dev/null || echo "     ℹ️  (ya existe)"

# Secret 2: MCP Client ID
echo "  → Creando secret: mcp-client-id"
aws secretsmanager create-secret \
    --name mcp-client-id \
    --secret-string "mcp-dev-client-id-abc123" \
    --description "Client ID para autenticación MCP" \
    --tags Key=Environment,Value=Development Key=Application,Value=mcp-bancolombia \
    --endpoint-url http://localhost:4566 \
    --region us-east-1 \
    2>/dev/null || echo "     ℹ️  (ya existe)"

# Secret 3: MCP Client Secret
echo "  → Creando secret: mcp-client-secret"
aws secretsmanager create-secret \
    --name mcp-client-secret \
    --secret-string "mcp-dev-client-secret-xyz789-super-secret" \
    --description "Client Secret para autenticación MCP" \
    --tags Key=Environment,Value=Development Key=Application,Value=mcp-bancolombia \
    --endpoint-url http://localhost:4566 \
    --region us-east-1 \
    2>/dev/null || echo "     ℹ️  (ya existe)"

# ============================================
# Verificar Secretos
# ============================================

echo ""
echo "✅ Secretos disponibles en Ministack:"
aws secretsmanager list-secrets \
    --query 'SecretList[*].[Name,Description]' \
    --output table \
    --endpoint-url http://localhost:4566 \
    --region us-east-1

echo ""
echo "=========================================="
echo " ✓ Inicialización completada"
echo "=========================================="
echo ""
echo "📌 Información de conexión:"
echo "   - Endpoint: http://localhost:4566"
echo "   - Region: us-east-1"
echo "   - Access Key: test"
echo "   - Secret Key: test"
echo "   - Ministack Docs: https://ministack.org/"
echo ""

