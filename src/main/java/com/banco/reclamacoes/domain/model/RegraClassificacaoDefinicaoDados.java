package com.banco.reclamacoes.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
public final class RegraClassificacaoDefinicaoDados {

    @NonNull private final CategoriaReclamacao categoria;
    @NonNull private final String termoNormalizado;
    private final int peso;
    @NonNull private final TipoTermoClassificacao tipo;
    @NonNull private final CriticidadeRegra criticidade;

    @Builder.Default
    private final String justificativa = "";
}
