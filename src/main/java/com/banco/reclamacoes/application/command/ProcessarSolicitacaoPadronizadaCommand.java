package com.banco.reclamacoes.application.command;

import com.banco.reclamacoes.domain.model.CanalOrigem;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProcessarSolicitacaoPadronizadaCommand(
    String schemaVersion,
    String solicitacaoId,
    String clienteId,
    CanalOrigem canalOrigem,
    String descricao,
    List<AnexoSolicitacaoCommand> anexos,
    Instant dataRecebimento,
    String correlationId,
    String idempotencyKey,
    Map<String, String> metadados
) {}
