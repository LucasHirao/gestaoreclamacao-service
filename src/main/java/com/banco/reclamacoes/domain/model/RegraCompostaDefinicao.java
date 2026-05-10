package com.banco.reclamacoes.domain.model;

import java.util.List;
import java.util.Objects;

public final class RegraCompostaDefinicao {

    private final CategoriaReclamacao categoria;
    private final List<String> termosObrigatoriosNormalizados;
    private final int pesoExtra;
    private final String justificativa;

    private RegraCompostaDefinicao(final RegraCompostaDefinicaoDados dados) {
        Objects.requireNonNull(dados);
        this.categoria = Objects.requireNonNull(dados.categoria());
        this.termosObrigatoriosNormalizados =
            List.copyOf(Objects.requireNonNull(dados.termosObrigatoriosNormalizados()));
        this.pesoExtra = dados.pesoExtra();
        this.justificativa = dados.justificativa() == null ? "" : dados.justificativa();
    }

    public static RegraCompostaDefinicao criar(final RegraCompostaDefinicaoDados dados) {
        return new RegraCompostaDefinicao(dados);
    }

    public CategoriaReclamacao categoria() {
        return categoria;
    }

    public List<String> termosObrigatoriosNormalizados() {
        return termosObrigatoriosNormalizados;
    }

    public int pesoExtra() {
        return pesoExtra;
    }

    public String justificativa() {
        return justificativa;
    }
}
