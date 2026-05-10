package com.banco.reclamacoes.infrastructure.observabilidade;

import com.banco.reclamacoes.application.port.output.HistoricoClientePort;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HistoricoClienteLoggingAdapter implements HistoricoClientePort {

    private static final Logger log = LoggerFactory.getLogger(HistoricoClienteLoggingAdapter.class);

    @Override
    public void registrarMovimentoCliente(final ClienteId clienteId, final String tipoMovimento, final String referencia) {
        log.info(
            "HISTORICO_CLIENTE clienteId={} tipo={} referencia={}",
            clienteId.valor(),
            tipoMovimento,
            referencia);
    }
}
