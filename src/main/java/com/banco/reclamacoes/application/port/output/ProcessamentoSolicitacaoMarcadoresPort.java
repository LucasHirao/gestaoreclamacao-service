package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;

public interface ProcessamentoSolicitacaoMarcadoresPort {

    void aposPrimeiraPersistencia(final Reclamacao salva, final ResultadoClassificacao resultadoDominio);
}
