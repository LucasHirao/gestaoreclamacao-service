package com.banco.reclamacoes.domain.event;

import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import java.time.Instant;

public record ReclamacaoCriada(
    ReclamacaoId reclamacaoId,
    SolicitacaoId solicitacaoId,
    ClienteId clienteId,
    Protocolo protocolo,
    CorrelationId correlationId,
    Instant ocorridoEm
) implements EventoDominio {
}
