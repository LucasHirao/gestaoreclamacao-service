package com.banco.reclamacoes.domain.model;

import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
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
public final class DadosNovaReclamacao {

    @NonNull private final SolicitacaoId solicitacaoId;
    @NonNull private final ClienteId clienteId;
    @NonNull private final CanalOrigem canalOrigem;
    @NonNull private final DescricaoReclamacao descricao;
    @NonNull private final Instant dataRecebimento;
    @NonNull private final CorrelationId correlationId;
    @NonNull private final IdempotencyKey idempotencyKey;

    @Builder.Default
    private final List<MetadadoAnexo> anexos = List.of();
}
