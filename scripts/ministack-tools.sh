#!/bin/bash

# ============================================
# Script de Utilidades - Ministack y Aplicación
# https://ministack.org/ | https://github.com/ministackorg/ministack
# ============================================
# Uso: ./scripts/ministack-tools.sh [comando]

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

AWS_ENDPOINT="http://localhost:4566"
AWS_REGION="us-east-1"

show_help() {
    cat << EOF
${BLUE}MCP Bancolombia - Ministack Tools${NC}

Uso: ./scripts/ministack-tools.sh [comando]

Comandos disponibles:
  ${GREEN}start${NC}              Inicia Ministack
  ${GREEN}stop${NC}               Detiene Ministack
  ${GREEN}restart${NC}            Reinicia Ministack
  ${GREEN}logs${NC}               Muestra logs de Ministack
  ${GREEN}status${NC}             Muestra estado de Ministack
  ${GREEN}create-secrets${NC}     Crea manualmente los secretos
  ${GREEN}list-secrets${NC}       Lista los secretos en Ministack
  ${GREEN}get-secret${NC} <name>  Obtiene el valor de un secret
  ${GREEN}delete-secret${NC} <name> Elimina un secret
  ${GREEN}reset${NC}              Limpia el estado de Ministack y re-ejecuta init scripts
  ${GREEN}build-app${NC}          Compila la aplicación
  ${GREEN}run-app${NC}            Ejecuta la aplicación con perfil dev
  ${GREEN}run-tests${NC}          Ejecuta los tests
  ${GREEN}dev-setup${NC}          Setup completo para desarrollo
  ${GREEN}dev-clean${NC}          Limpia build y reinicia entorno
  ${GREEN}help${NC}               Muestra esta ayuda
EOF
}

aws_cmd() {
    docker compose exec -T ministack aws "$@" --endpoint-url "$AWS_ENDPOINT" --region "$AWS_REGION"
}

start_ministack() {
    echo -e "${BLUE}🚀 Iniciando Ministack...${NC}"
    docker compose up -d
    sleep 5
    echo -e "${GREEN}✅ Ministack iniciado${NC}"
    echo -e "   Endpoint: $AWS_ENDPOINT"
    echo -e "   Región: $AWS_REGION"
    echo -e "   Health: $AWS_ENDPOINT/_ministack/health"
}

stop_ministack() {
    echo -e "${BLUE}🛑 Deteniendo Ministack...${NC}"
    docker compose down
    echo -e "${GREEN}✅ Ministack detenido${NC}"
}

restart_ministack() {
    stop_ministack
    sleep 2
    start_ministack
}

show_logs() {
    docker compose logs -f ministack
}

show_status() {
    echo -e "${BLUE}📊 Estado de Ministack${NC}"
    docker compose ps
}

create_secrets_manual() {
    echo -e "${BLUE}📝 Creando secretos en Ministack...${NC}"

    aws_cmd secretsmanager create-secret \
        --name api-consumer-key \
        --secret-string "simpsons-api-key-dev-12345" \
        --description "API Consumer Key para integración con APIs externas" \
        2>/dev/null || echo "   (api-consumer-key ya existe)"

    aws_cmd secretsmanager create-secret \
        --name mcp-client-id \
        --secret-string "mcp-dev-client-id-abc123" \
        --description "Client ID para autenticación MCP" \
        2>/dev/null || echo "   (mcp-client-id ya existe)"

    aws_cmd secretsmanager create-secret \
        --name mcp-client-secret \
        --secret-string "mcp-dev-client-secret-xyz789-super-secret" \
        --description "Client Secret para autenticación MCP" \
        2>/dev/null || echo "   (mcp-client-secret ya existe)"

    echo -e "${GREEN}✅ Secretos creados${NC}"
}

list_secrets() {
    echo -e "${BLUE}📋 Secretos en Ministack${NC}"
    aws_cmd secretsmanager list-secrets \
        --query 'SecretList[*].[Name,Description]' \
        --output table
}

get_secret_value() {
    local secret_name=$1
    if [ -z "$secret_name" ]; then
        echo -e "${RED}❌ Debe especificar el nombre del secret${NC}"
        return 1
    fi

    echo -e "${BLUE}🔐 Obteniendo valor del secret: ${YELLOW}$secret_name${NC}"
    aws_cmd secretsmanager get-secret-value \
        --secret-id "$secret_name" \
        --query 'SecretString' \
        --output text
    echo ""
}

delete_secret_value() {
    local secret_name=$1
    if [ -z "$secret_name" ]; then
        echo -e "${RED}❌ Debe especificar el nombre del secret${NC}"
        return 1
    fi

    echo -e "${YELLOW}⚠️  Eliminando secret: $secret_name${NC}"
    aws_cmd secretsmanager delete-secret \
        --secret-id "$secret_name" \
        --force-delete-without-recovery
    echo -e "${GREEN}✅ Secret eliminado${NC}"
}

reset_ministack() {
    echo -e "${YELLOW}♻️  Reinicializando estado de Ministack...${NC}"
    curl -fsS "$AWS_ENDPOINT/_ministack/reset?init=1" > /dev/null
    echo -e "${GREEN}✅ Estado reiniciado y scripts de init ejecutados${NC}"
}

build_app() {
    echo -e "${BLUE}🔨 Compilando aplicación...${NC}"
    ./gradlew build -x pitest
    echo -e "${GREEN}✅ Compilación exitosa${NC}"
}

run_app() {
    echo -e "${BLUE}🚀 Ejecutando aplicación (perfil dev)...${NC}"
    export SPRING_PROFILES_ACTIVE=dev
    ./gradlew :app-service:bootRun
}

run_tests() {
    echo -e "${BLUE}🧪 Ejecutando tests...${NC}"
    ./gradlew test -x pitest
    echo -e "${GREEN}✅ Tests completados${NC}"
}

dev_setup() {
    echo -e "${BLUE}⚙️  Setup de desarrollo completo con Ministack...${NC}"
    start_ministack
    sleep 5
    create_secrets_manual
    echo -e "${GREEN}✅ Setup completado${NC}"
}

dev_clean() {
    echo -e "${YELLOW}⚠️  Limpiando build y reiniciando entorno...${NC}"
    stop_ministack
    sleep 2
    rm -rf build build-cache
    dev_setup
}

COMMAND=${1:-help}
case "$COMMAND" in
    start) start_ministack ;;
    stop) stop_ministack ;;
    restart) restart_ministack ;;
    logs) show_logs ;;
    status) show_status ;;
    create-secrets) create_secrets_manual ;;
    list-secrets) list_secrets ;;
    get-secret) get_secret_value "$2" ;;
    delete-secret) delete_secret_value "$2" ;;
    reset) reset_ministack ;;
    build-app) build_app ;;
    run-app) run_app ;;
    run-tests) run_tests ;;
    dev-setup) dev_setup ;;
    dev-clean) dev_clean ;;
    help|--help|-h) show_help ;;
    *)
        echo -e "${RED}❌ Comando no reconocido: $COMMAND${NC}"
        show_help
        exit 1
        ;;
esac

