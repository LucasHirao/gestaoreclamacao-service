package com.banco.reclamacoes.infrastructure.web.dto.dev;

import com.banco.reclamacoes.application.command.AnexoSolicitacaoCommand;
import com.banco.reclamacoes.application.command.ProcessarSolicitacaoPadronizadaCommand;
import com.banco.reclamacoes.domain.model.CanalOrigem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Schema(description = "Mensagem padronizada vinda dos canais (POC: metadados em snake_case, texto ≥20 caracteres)")
public record SolicitacaoPadronizadaInboundDto(
        String schemaVersion,
        String solicitacaoId,
        String clienteId,
        CanalOrigem canalOrigem,
        String descricao,
        List<AnexoInboundDto> anexos,
        Instant dataRecebimento,
        String correlationId,
        String idempotencyKey,
        Map<String, String> metadados
) {

    public ProcessarSolicitacaoPadronizadaCommand paraComando() {
        List<AnexoSolicitacaoCommand> comandos =
                anexos == null
                        ? List.of()
                        : anexos.stream()
                        .map(
                                a ->
                                        new AnexoSolicitacaoCommand( a.referencia(), a.tipoMime(), a.atributos() == null ? Map.of() : a.atributos()))
                        .toList();
        return new ProcessarSolicitacaoPadronizadaCommand( schemaVersion, solicitacaoId, clienteId, canalOrigem, descricao, comandos, dataRecebimento, correlationId, idempotencyKey, metadados == null ? Map.of() : metadados);
    }
}
