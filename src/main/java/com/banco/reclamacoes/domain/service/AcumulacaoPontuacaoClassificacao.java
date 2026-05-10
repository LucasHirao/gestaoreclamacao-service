package com.banco.reclamacoes.domain.service;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/** Acumula pontos por categoria e justificativas durante a passagem única da classificação. */
final class AcumulacaoPontuacaoClassificacao {

    private final EnumMap<CategoriaReclamacao, Integer> porCategoria;
    private final List<String> justificativas;

    AcumulacaoPontuacaoClassificacao() {
        porCategoria = new EnumMap<>(CategoriaReclamacao.class);
        for (final var cat : CategoriaReclamacao.values()) {
            porCategoria.put(cat, 0);
        }
        justificativas = new ArrayList<>();
    }

    void contribuir(final CategoriaReclamacao categoria, final int peso, final String textoJustificativa) {
        porCategoria.merge(categoria, peso, Integer::sum);
        if (textoJustificativa != null && !textoJustificativa.isBlank()) {
            justificativas.add(textoJustificativa);
        }
    }

    EnumMap<CategoriaReclamacao, Integer> porCategoria() {
        return porCategoria;
    }

    List<String> justificativas() {
        return justificativas;
    }
}
