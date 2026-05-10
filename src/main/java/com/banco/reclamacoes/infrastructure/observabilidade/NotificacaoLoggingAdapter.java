package com.banco.reclamacoes.infrastructure.observabilidade;

import com.banco.reclamacoes.application.port.output.NotificacaoPort;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoLoggingAdapter implements NotificacaoPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoLoggingAdapter.class);

    @Override
    public void notificarAlertaSla(final ReclamacaoId reclamacaoId, final Protocolo protocolo, final Instant referenciaTemporal) {
        log.warn(
            "ALERTA_SLA_NOTIFICACAO reclamacaoId={} protocolo={} referencia={}",
            reclamacaoId.valor(),
            protocolo.valor(),
            referenciaTemporal);
    }
}
