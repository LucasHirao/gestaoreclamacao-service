package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.valueobject.ClienteId;

public interface HistoricoClientePort {

    /**
     * Ponto de extensão futuro para registros no histórico materializado do cliente.
     * No MVP apenas registra no log via adaptador infra.
     */
    void registrarMovimentoCliente(
        final ClienteId clienteId, final String tipoMovimento, final String referencia);
}
