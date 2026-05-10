package com.banco.reclamacoes.application.port.output;

public interface PublicadorIntegracaoPort {

    void publicar(final IntegracaoPublicacaoComando comando);
}
