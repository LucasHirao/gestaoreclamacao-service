package com.banco.reclamacoes.domain.event;

import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Instant;

public record AlertaSlaEmitido(
    ReclamacaoId reclamacaoId,
    Protocolo protocolo,
    Instant ocorridoEm
) implements EventoDominio {
}
