package com.banco.reclamacoes.domain.port;

import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;

/** Abstrai a geração de identificadores únicos para novas reclamações. */
public interface ProvedorIdentidadeReclamacao {

    ReclamacaoId novaIdentidade();
}
