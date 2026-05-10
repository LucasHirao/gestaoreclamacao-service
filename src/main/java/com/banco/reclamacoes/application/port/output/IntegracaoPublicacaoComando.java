package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
public final class IntegracaoPublicacaoComando {

    @NonNull private final ReclamacaoId reclamacaoId;
    @NonNull private final Protocolo protocolo;
    @NonNull private final ClienteId clienteId;
    @NonNull private final CategoriaReclamacao categoria;
    @NonNull private final StatusReclamacao status;
    @NonNull private final Instant dataRecebimento;
    @NonNull private final CorrelationId correlationId;
}
