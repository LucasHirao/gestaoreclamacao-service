package com.banco.reclamacoes.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.model.CriticidadeRegra;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ScoreClassificacao;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PoliticaConfiancaClassificacaoTest {

    @Test
    void confiancaAltaParaScoreElevadoSegundoLugarDistancia() {
        Map<CategoriaReclamacao, ScoreClassificacao> mapa = zerarTodos();
        mapa.put(CategoriaReclamacao.COBRANCA, ScoreClassificacao.de(20));
        mapa.put(CategoriaReclamacao.CARTAO, ScoreClassificacao.de(10));

        ResultadoClassificacao resultado =
            PoliticaConfiancaClassificacao.aplicar(mapa, List.of(), ScoreClassificacao.de(0));

        assertThat(resultado.confianca()).isEqualTo(ConfiancaClassificacao.ALTA);
    }

    @Test
    void empateCriticoEntreRegulatoriosComPontuacaoAltaElevaParaMediaEMarcaManual() {
        Map<CategoriaReclamacao, ScoreClassificacao> mapa = zerarTodos();
        mapa.put(CategoriaReclamacao.FRAUDE, ScoreClassificacao.de(22));
        mapa.put(CategoriaReclamacao.OUVIDORIA_RECLAMACAO_REGULATORIA, ScoreClassificacao.de(22));

        ResultadoClassificacao resultado =
            PoliticaConfiancaClassificacao.aplicar(mapa, List.of(), ScoreClassificacao.de(22));

        assertThat(resultado.confianca()).isEqualTo(ConfiancaClassificacao.MEDIA);
        assertThat(resultado.requerRevisaoManual()).isTrue();
    }

    @Test
    void fraudeComScoreAltoImpedeConfiancaApenasBaixa() {
        Map<CategoriaReclamacao, ScoreClassificacao> mapa = zerarTodos();
        mapa.put(CategoriaReclamacao.FRAUDE, ScoreClassificacao.de(16));
        mapa.put(CategoriaReclamacao.CARTAO, ScoreClassificacao.de(14));

        ResultadoClassificacao resultado =
            PoliticaConfiancaClassificacao.aplicar(mapa, List.of(), ScoreClassificacao.de(16));

        assertThat(resultado.confianca()).isEqualTo(ConfiancaClassificacao.MEDIA);
    }

    @Test
    void fallbackOutrosQuandoInexistenteRelevancia() {
        Map<CategoriaReclamacao, ScoreClassificacao> mapa = zerarTodos();

        ResultadoClassificacao resultado =
            PoliticaConfiancaClassificacao.aplicar(mapa, List.of(), ScoreClassificacao.de(0));

        assertThat(resultado.categoriaPrincipal()).isEqualTo(CategoriaReclamacao.OUTROS);
        assertThat(resultado.confianca()).isEqualTo(ConfiancaClassificacao.BAIXA);
    }

    private static Map<CategoriaReclamacao, ScoreClassificacao> zerarTodos() {
        final var mapa = new EnumMap<CategoriaReclamacao, ScoreClassificacao>(CategoriaReclamacao.class);
        for (final var c : CategoriaReclamacao.values()) {
            mapa.put(c, ScoreClassificacao.de(0));
        }
        return mapa;
    }

    @Test
    void acumulaCriticidadeMaior() {
        java.util.Map<CategoriaReclamacao, CriticidadeRegra> acumulado =
            new EnumMap<>(CategoriaReclamacao.class);
        acumulado =
            PoliticaConfiancaClassificacao.maiorCriticidadeAcumulada(
                acumulado, CategoriaReclamacao.FRAUDE, CriticidadeRegra.MEDIA);
        acumulado =
            PoliticaConfiancaClassificacao.maiorCriticidadeAcumulada(
                acumulado, CategoriaReclamacao.FRAUDE, CriticidadeRegra.ALTA);
        assertThat(acumulado.get(CategoriaReclamacao.FRAUDE)).isEqualTo(CriticidadeRegra.ALTA);
    }
}
