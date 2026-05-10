package com.banco.reclamacoes.domain.event;

import com.banco.reclamacoes.domain.valueobject.PrazoSla;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Instant;

public record SlaCalculado(
    ReclamacaoId reclamacaoId,
    PrazoSla deadline,
    PrazoSla alerta,
    Instant ocorridoEm
) implements EventoDominio {
}
