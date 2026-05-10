package com.banco.reclamacoes.domain.model;

import java.util.Objects;

public final class RegraClassificacaoDefinicao {

    private final CategoriaReclamacao categoria;
    private final String termoNormalizado;
    private final int peso;
    private final TipoTermoClassificacao tipo;
    private final CriticidadeRegra criticidade;
    private final String justificativa;

    private RegraClassificacaoDefinicao(final RegraClassificacaoDefinicaoDados dados) {
        Objects.requireNonNull(dados);
        this.categoria = Objects.requireNonNull(dados.categoria());
        this.termoNormalizado = Objects.requireNonNull(dados.termoNormalizado());
        this.peso = dados.peso();
        this.tipo = Objects.requireNonNull(dados.tipo());
        this.criticidade = Objects.requireNonNull(dados.criticidade());
        this.justificativa = dados.justificativa() == null ? "" : dados.justificativa();
    }

    public static RegraClassificacaoDefinicao criar(final RegraClassificacaoDefinicaoDados dados) {
        return new RegraClassificacaoDefinicao(dados);
    }

    public CategoriaReclamacao categoria() {
        return categoria;
    }

    public String termoNormalizado() {
        return termoNormalizado;
    }

    public int peso() {
        return peso;
    }

    public TipoTermoClassificacao tipo() {
        return tipo;
    }

    public CriticidadeRegra criticidade() {
        return criticidade;
    }

    public String justificativa() {
        return justificativa;
    }
}
