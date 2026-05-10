#!/usr/bin/env bash
set -euo pipefail
# Demonstração rápida com profile local: suba a aplicação com
#   SPRING_PROFILES_ACTIVE=local \
#   AWS_ENDPOINT_URL=http://localhost:4566 \
#   SQS_SOLICITACAO_PADRONIZADA=http://localhost:4566/000000000000/solicitacao-padronizada \
#   SQS_INTEGRACAO=http://localhost:4566/000000000000/integracao-reclamacoes
#   SQS_SLA_VERIFICACAO=http://localhost:4566/000000000000/sla-verificacao
#
BASE="${BASE:-http://localhost:8080}"
echo "POST classificação (dev)…"
curl -sS -X POST "$BASE/dev/classificar-texto" \
  -H 'Content-Type: application/json' \
  -d '{"texto":"não reconheço uma compra no meu cartão"}' | head -c 400
echo
echo "POST simular solicitação (dev)…"
curl -sS -X POST "$BASE/dev/simular-solicitacao-padronizada" \
  -H 'Content-Type: application/json' \
  -d '{
    "schemaVersion":"1",
    "solicitacaoId":"SOL-DEMO-001",
    "clienteId":"CLI-DEMO",
    "canalOrigem":"DIGITAL",
    "descricao":"Relato longo suficiente para validar processamento sem documento fiscal.",
    "anexos":[],
    "dataRecebimento":"2026-05-10T12:00:00Z",
    "correlationId":"COR-DEMO",
    "idempotencyKey":"idem-demo-001",
    "metadados":{"canal":"internet_banking"}
  }' | head -c 200
echo
echo "Métricas Prometheus: $BASE/actuator/prometheus (trecho)"
curl -sS "$BASE/actuator/prometheus" | head -n 5
