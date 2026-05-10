package com.banco.reclamacoes.infrastructure.messaging;

@SuppressWarnings({"java:S100", "java:S116"})
record MensagemIntegracaoPayload(
    String reclamacaoId,
    String protocolo,
    String clienteId,
    String categoria,
    String status,
    String dataRecebimento,
    String correlationId
) {}
