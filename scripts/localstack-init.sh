#!/bin/sh
set -e
awslocal sqs create-queue --queue-name solicitacao-padronizada || true
awslocal sqs create-queue --queue-name integracao-reclamacoes || true
awslocal sqs create-queue --queue-name sla-verificacao || true
awslocal sns create-topic --name integracao-eventos || true
awslocal dynamodb create-table \
  --table-name bdd-fronteira-sanidade \
  --attribute-definitions AttributeName=pk,AttributeType=S \
  --key-schema AttributeName=pk,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST || true
