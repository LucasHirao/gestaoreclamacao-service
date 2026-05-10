package com.banco.reclamacoes.domain.port;

import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;

/**
 * Porta de saída: serviço de classificação automática da reclamação (implementação na infraestrutura/aplicação).
 */
public interface MotorClassificacaoPort {

    ResultadoClassificacao classificar(final DescricaoReclamacao descricao);
}
