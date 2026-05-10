package com.banco.reclamacoes.domain.model;

import com.banco.reclamacoes.domain.valueobject.Classificacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.Sla;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
public final class ReclamacaoPersistenciaSnapshot {

    @NonNull private final ReclamacaoId id;
    @NonNull private final SolicitacaoId solicitacaoId;
    @NonNull private final Protocolo protocolo;
    @NonNull private final ClienteId clienteId;
    @NonNull private final CanalOrigem canalOrigem;
    @NonNull private final DescricaoReclamacao descricao;
    @NonNull private final StatusReclamacao status;
    private final Classificacao classificacao;
    private final ResultadoClassificacao resultadoClassificacao;
    @NonNull private final Instant dataRecebimento;
    private final Sla sla;
    private final boolean alertaSlaEmitido;
    private final boolean integracaoRegistrada;
    @NonNull private final CorrelationId correlationId;
    @NonNull private final IdempotencyKey idempotencyKey;
    @NonNull private final List<MetadadoAnexo> anexos;
    @NonNull private final List<HistoricoInterno> historicosPersistidos;
}
