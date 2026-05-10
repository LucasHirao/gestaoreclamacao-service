package com.banco.reclamacoes.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.CriticidadeRegra;
import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicao;
import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicaoDados;
import com.banco.reclamacoes.domain.model.TipoTermoClassificacao;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClassificadorPorRegrasPonderadasTest {

    /**
     * Exercita regex compilada no loop de matcher (caminho opcional quando não há regras {@code REGEX} no JSON).
     */
    @Test
    void classificacaoCombinaPontuacaoPorRegexNormalizado() {
        final var dadosNfe =
            RegraClassificacaoDefinicaoDados.builder()
                .categoria(CategoriaReclamacao.OUTROS)
                .termoNormalizado("(?i)nfe\\s*\\d{4}")
                .peso(40)
                .tipo(TipoTermoClassificacao.REGEX)
                .criticidade(CriticidadeRegra.ALTA)
                .justificativa("match regex nfe yyyy")
                .build();
        final var regraNfe = RegraClassificacaoDefinicao.criar(dadosNfe);

        ClassificadorPorRegrasPonderadas classificador =
            ClassificadorPorRegrasPonderadas.montar(List.of(regraNfe), List.of(), Map.of());

        var resultado =
            classificador.classificar(
                DescricaoReclamacao.de(
                    "Este relato é longo o suficiente: preciso de suporte sobre nfe 2024 no aplicativo."));
        assertThat(resultado.justificativas()).anyMatch(j -> j.contains("regex nfe"));
    }
}
