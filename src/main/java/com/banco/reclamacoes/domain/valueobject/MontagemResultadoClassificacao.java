package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Builder
@Accessors(fluent = true)
public final class MontagemResultadoClassificacao {

    @NonNull private final CategoriaReclamacao categoriaPrincipal;
    @NonNull private final List<CategoriaPontuacao> categoriasCandidatas;
    @NonNull private final ScoreClassificacao scorePrincipal;
    @NonNull private final ConfiancaClassificacao confianca;
    @NonNull private final List<String> justificativas;
    private final boolean requerRevisaoManual;
}
