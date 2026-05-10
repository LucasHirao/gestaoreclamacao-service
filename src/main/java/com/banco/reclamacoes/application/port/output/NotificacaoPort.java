package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Instant;

public interface NotificacaoPort {
    void notificarAlertaSla(
        final ReclamacaoId reclamacaoId,
        final Protocolo protocolo,
        final Instant referenciaTemporal);
}
