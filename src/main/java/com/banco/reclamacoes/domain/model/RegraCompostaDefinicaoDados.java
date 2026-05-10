package com.banco.reclamacoes.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
public final class RegraCompostaDefinicaoDados {

    @NonNull private final CategoriaReclamacao categoria;
    @NonNull private final List<String> termosObrigatoriosNormalizados;
    private final int pesoExtra;

    @Builder.Default
    private final String justificativa = "";
}
