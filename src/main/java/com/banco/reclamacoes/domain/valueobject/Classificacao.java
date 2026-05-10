package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import java.util.Objects;

public final class Classificacao {

    private final CategoriaReclamacao categoriaPrincipal;
    private final ConfiancaClassificacao confianca;

    private Classificacao(final CategoriaReclamacao categoriaPrincipal, final ConfiancaClassificacao confianca) {
        this.categoriaPrincipal = Objects.requireNonNull(categoriaPrincipal);
        this.confianca = Objects.requireNonNull(confianca);
    }

    public static Classificacao de(final CategoriaReclamacao categoriaPrincipal, final ConfiancaClassificacao confianca) {
        return new Classificacao(categoriaPrincipal, confianca);
    }

    public CategoriaReclamacao categoriaPrincipal() {
        return categoriaPrincipal;
    }

    public ConfiancaClassificacao confianca() {
        return confianca;
    }
}
