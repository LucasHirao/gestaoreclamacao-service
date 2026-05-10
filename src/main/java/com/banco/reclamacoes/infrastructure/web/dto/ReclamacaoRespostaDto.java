package com.banco.reclamacoes.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReclamacaoRespostaDto(
    UUID reclamacaoId,
    String solicitacaoId,
    String protocolo,
    String clienteId,
    String canalOrigem,
    String descricao,
    String status,
    ResultadoClassificacaoRespostaDto classificacao,
    Instant deadlineAt,
    Instant alertAt,
    boolean alertaEmitido,
    boolean integracaoPublicada,
    String correlationId,
    String idempotencyKey,
    Instant dataRecebimento,
    List<HistoricoLinhaDto> historicoInterno
) {}
