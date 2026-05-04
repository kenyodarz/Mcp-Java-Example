#!/bin/bash

# ============================================
# ⚠️  DEPRECATED - Este script ha sido deprecado
# ============================================
# Este script ya no se utiliza. Para crear secretos en Ministack,
# siga las instrucciones en: docs/guides/manual-secrets-setup.md
#
# Razón de deprecación:
# - El script falla silenciosamente sin error visible
# - No proporciona logs útiles para debugging
# - Es mejor usar comandos manuales con mejor visibilidad
#
# Para crear secretos, ejecute una de estas opciones:
#
#   Opción 1 (Recomendado - AWS CLI local):
#     export AWS_ENDPOINT=http://localhost:4566
#     aws secretsmanager create-secret \
#       --name api-consumer-key \
#       --secret-string "simpsons-api-key-dev-12345" \
#       --endpoint-url "$AWS_ENDPOINT" --region us-east-1
#
#   Opción 2 (Docker Compose):
#     docker compose exec -T ministack aws secretsmanager create-secret \
#       --name api-consumer-key \
#       --secret-string "simpsons-api-key-dev-12345" \
#       --endpoint-url "http://localhost:4566" --region us-east-1
#
#   Opción 3 (Script de utilidades):
#     ./scripts/ministack-tools.sh create-secrets
#
# Ver completo: docs/guides/manual-secrets-setup.md
# ============================================

exit 0
