package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class ScoreClassificacao {

    private final int pontos;

    private ScoreClassificacao( int pontos) {
        if (pontos < 0) {
            throw new ViolacaoDominioException("score de classificação não pode ser negativo");
        }
        this.pontos = pontos;
    }

    public static ScoreClassificacao de( int pontos) {
        return new ScoreClassificacao(pontos);
    }

    public int pontos() {
        return pontos;
    }

    public ScoreClassificacao somar( int delta) {
        return new ScoreClassificacao(this.pontos + delta);
    }

    public ScoreClassificacao somar(final ScoreClassificacao other) {
        return new ScoreClassificacao(this.pontos + other.pontos);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScoreClassificacao that = (ScoreClassificacao) o;
        return pontos == that.pontos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pontos);
    }
}
