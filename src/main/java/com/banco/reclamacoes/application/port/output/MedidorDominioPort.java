package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;

public interface MedidorDominioPort {

    void registrarCriacaoReclamacao();

    void registrarClassificacaoBaixaConfianca();

    void registrarTempoClassificacaoNanos(final long nanos);

    void registrarAlertaSlaEmitido();

    void registrarIntegracaoPublicada(final CategoriaReclamacao categoria);

    void registrarErroProcessamento();
}
