package com.banco.reclamacoes.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@SuppressWarnings({"java:S100", "java:S116"})
class ResultadoClassificacaoPersistivel {

    String categoriaPrincipal;
    List<Map<String, Object>> categoriasCandidatas = new ArrayList<>();
    int scorePrincipal;
    ConfiancaClassificacao confianca;
    List<String> justificativas = new ArrayList<>();
    boolean requerRevisaoManual;

    ResultadoClassificacaoPersistivel() {}

    ResultadoClassificacaoPersistivel(
        final String categoriaPrincipal,
        final List<Map<String, Object>> categoriasCandidatas,
        final int scorePrincipal,
        final ConfiancaClassificacao confianca,
        final List<String> justificativas,
        final boolean requerRevisaoManual
    ) {
        this.categoriaPrincipal = categoriaPrincipal;
        this.categoriasCandidatas = categoriasCandidatas;
        this.scorePrincipal = scorePrincipal;
        this.confianca = confianca;
        this.justificativas = justificativas;
        this.requerRevisaoManual = requerRevisaoManual;
    }

    static ResultadoClassificacaoPersistivel of(final ResultadoClassificacao dominio) {
        final var cand = new ArrayList<Map<String, Object>>();
        for (final var pontuacao : dominio.categoriasCandidatas()) {
            final var entrada = new LinkedHashMap<String, Object>();
            entrada.put("categoria", pontuacao.categoria().name());
            entrada.put("score", pontuacao.score().pontos());
            cand.add(entrada);
        }
        return new ResultadoClassificacaoPersistivel( dominio.categoriaPrincipal().name(), cand, dominio.scorePrincipal().pontos(), dominio.confianca(), new ArrayList<>(dominio.justificativas()), dominio.requerRevisaoManual());
    }

    CategoriaReclamacao categoriaPrincipalEnum() {
        return CategoriaReclamacao.valueOf(categoriaPrincipal);
    }
}
