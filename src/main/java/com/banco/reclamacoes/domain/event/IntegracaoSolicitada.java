package com.banco.reclamacoes.domain.event;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Instant;

public record IntegracaoSolicitada(
    ReclamacaoId reclamacaoId,
    Protocolo protocolo,
    ClienteId clienteId,
    CategoriaReclamacao categoria,
    StatusReclamacao status,
    Instant dataRecebimento,
    CorrelationId correlationId,
    Instant ocorridoEm
) implements EventoDominio {
}
