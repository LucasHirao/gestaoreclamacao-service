package com.banco.reclamacoes.infrastructure.classification;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.CriticidadeRegra;
import com.banco.reclamacoes.domain.model.TipoTermoClassificacao;

class RegraDocumentoEntradaJson {
    public CategoriaReclamacao categoria;
    public String termo;
    public int peso;
    public TipoTermoClassificacao tipo;
    public CriticidadeRegra criticidade;
    public String justificativa;
}
