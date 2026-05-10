package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import java.util.Objects;

public final class CategoriaPontuacao {

    private final CategoriaReclamacao categoria;
    private final ScoreClassificacao score;

    private CategoriaPontuacao(final CategoriaReclamacao categoria, final ScoreClassificacao score) {
        this.categoria = Objects.requireNonNull(categoria);
        this.score = Objects.requireNonNull(score);
    }

    public static CategoriaPontuacao de(final CategoriaReclamacao categoria, final ScoreClassificacao score) {
        return new CategoriaPontuacao(categoria, score);
    }

    public CategoriaReclamacao categoria() {
        return categoria;
    }

    public ScoreClassificacao score() {
        return score;
    }
}
