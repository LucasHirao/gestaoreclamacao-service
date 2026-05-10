package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ResultadoClassificacao {

    private final CategoriaReclamacao categoriaPrincipal;
    private final List<CategoriaPontuacao> categoriasCandidatas;
    private final ScoreClassificacao scorePrincipal;
    private final ConfiancaClassificacao confianca;
    private final List<String> justificativas;
    private final boolean requerRevisaoManual;

    private ResultadoClassificacao(final MontagemResultadoClassificacao montagem) {
        Objects.requireNonNull(montagem);
        this.categoriaPrincipal = Objects.requireNonNull(montagem.categoriaPrincipal());
        this.categoriasCandidatas = List.copyOf(Objects.requireNonNull(montagem.categoriasCandidatas()));
        this.scorePrincipal = Objects.requireNonNull(montagem.scorePrincipal());
        this.confianca = Objects.requireNonNull(montagem.confianca());
        this.justificativas = List.copyOf(Objects.requireNonNull(montagem.justificativas()));
        this.requerRevisaoManual = montagem.requerRevisaoManual();
    }

    public static ResultadoClassificacao montar(final MontagemResultadoClassificacao montagem) {
        return new ResultadoClassificacao(montagem);
    }

    public CategoriaReclamacao categoriaPrincipal() {
        return categoriaPrincipal;
    }

    public List<CategoriaPontuacao> categoriasCandidatas() {
        return Collections.unmodifiableList(categoriasCandidatas);
    }

    public ScoreClassificacao scorePrincipal() {
        return scorePrincipal;
    }

    public ConfiancaClassificacao confianca() {
        return confianca;
    }

    public List<String> justificativas() {
        return Collections.unmodifiableList(justificativas);
    }

    public boolean requerRevisaoManual() {
        return requerRevisaoManual;
    }
}
