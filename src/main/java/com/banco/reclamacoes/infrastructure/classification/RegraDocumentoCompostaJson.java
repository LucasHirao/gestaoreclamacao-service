package com.banco.reclamacoes.infrastructure.classification;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import java.util.List;

class RegraDocumentoCompostaJson {
    public CategoriaReclamacao categoria;
    public List<String> termosObrigatorios;
    public int pesoExtra;
    public String justificativa;
}
