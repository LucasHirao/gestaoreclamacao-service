package com.banco.reclamacoes.domain.policy;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.CriticidadeRegra;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.valueobject.CategoriaPontuacao;
import com.banco.reclamacoes.domain.valueobject.MontagemResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ScoreClassificacao;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PoliticaConfiancaClassificacao {

    private static final Set<CategoriaReclamacao> CATEGORIAS_CRITICAS = EnumSet.of(
        CategoriaReclamacao.FRAUDE,
        CategoriaReclamacao.OUVIDORIA_RECLAMACAO_REGULATORIA);

    private PoliticaConfiancaClassificacao() {}

    public static ResultadoClassificacao aplicar(
            final Map<CategoriaReclamacao, ScoreClassificacao> pontosPorCategoria,
            final List<String> justificativas,
            final ScoreClassificacao scoreFraudeAcumulado) {
        final var ranking = ordenar(pontosPorCategoria);
        final var semRelevante = ranking.isEmpty()
            || (ranking.size() == 1 && ranking.getFirst().score().pontos() == 0)
            || ranking.stream().noneMatch(c -> c.score().pontos() > 0);

        if (semRelevante) {
            final var montagemBaixaDenominacao =
                MontagemResultadoClassificacao.builder()
                    .categoriaPrincipal(CategoriaReclamacao.OUTROS)
                    .categoriasCandidatas(
                        List.of(CategoriaPontuacao.de(CategoriaReclamacao.OUTROS, ScoreClassificacao.de(0))))
                    .scorePrincipal(ScoreClassificacao.de(0))
                    .confianca(ConfiancaClassificacao.BAIXA)
                    .justificativas(aplicarTruncamento(justificativas))
                    .requerRevisaoManual(false)
                    .build();
            return ResultadoClassificacao.montar(montagemBaixaDenominacao);
        }

        final var primeira = ranking.getFirst();
        final var segunda = segundaDistinta(primeira, ranking);

        final var s1 = primeira.score().pontos();
        final var s2 = segunda.score().pontos();
        final var diferenca = s1 - s2;

        final var empateTopo = ranking.stream().filter(c -> c.score().pontos() == s1).count() > 1;
        final var empateCritico = empateTopo && maisDeUmaCriticaNoEmpate(ranking, s1);

        var confiancaBase = avaliarFaixa(s1, diferenca, empateCritico);
        if (scoreFraudeAcumulado.pontos() >= 12) {
            confiancaBase = maior(confiancaBase, ConfiancaClassificacao.MEDIA);
        }

        final var requerRevisaoManual = empateCritico || confiancaBase == ConfiancaClassificacao.BAIXA;

        final var candidatas =
            ranking.stream().filter(c -> c.score().pontos() > 0).limit(6).toList();

        final var montagem =
            MontagemResultadoClassificacao.builder()
                .categoriaPrincipal(primeira.categoria())
                .categoriasCandidatas(candidatas.isEmpty() ? List.of(primeira) : candidatas)
                .scorePrincipal(primeira.score())
                .confianca(confiancaBase)
                .justificativas(aplicarTruncamento(justificativas))
                .requerRevisaoManual(requerRevisaoManual)
                .build();
        return ResultadoClassificacao.montar(montagem);
    }

    private static CategoriaPontuacao segundaDistinta(
            final CategoriaPontuacao primeira, final List<CategoriaPontuacao> ranking) {
        return ranking.stream()
            .filter(c -> c.categoria() != primeira.categoria())
            .findFirst()
            .orElse(CategoriaPontuacao.de(primeira.categoria(), ScoreClassificacao.de(0)));
    }

    private static List<CategoriaPontuacao> ordenar(
            final Map<CategoriaReclamacao, ScoreClassificacao> pontosPorCategoria) {
        final var lista = new ArrayList<CategoriaPontuacao>();
        pontosPorCategoria.forEach((cat, sc) -> lista.add(CategoriaPontuacao.de(cat, sc)));
        lista.sort(Comparator.comparingInt((CategoriaPontuacao c) -> c.score().pontos()).reversed()
            .thenComparing(c -> c.categoria().name()));
        return lista;
    }

    private static ConfiancaClassificacao avaliarFaixa(
        final int scorePrincipal, final int diferenca, final boolean empateCritico) {
        if (empateCritico) {
            return ConfiancaClassificacao.BAIXA;
        }
        if (scorePrincipal >= 18 && diferenca >= 6) {
            return ConfiancaClassificacao.ALTA;
        }
        if (scorePrincipal >= 10 && diferenca >= 3) {
            return ConfiancaClassificacao.MEDIA;
        }
        return ConfiancaClassificacao.BAIXA;
    }

    private static boolean maisDeUmaCriticaNoEmpate(
            final List<CategoriaPontuacao> ranking, final int scoreTopo) {
        final var empatadas = ranking.stream()
            .filter(c -> c.score().pontos() == scoreTopo)
            .map(CategoriaPontuacao::categoria)
            .toList();
        final var criticas =
            empatadas.stream().filter(CATEGORIAS_CRITICAS::contains).count();
        return criticas >= 2;
    }

    private static ConfiancaClassificacao maior(
            final ConfiancaClassificacao a, final ConfiancaClassificacao b) {
        final var ordemA = ordinalConfianca(a);
        final var ordemB = ordinalConfianca(b);
        return ordemA >= ordemB ? a : b;
    }

    private static int ordinalConfianca(final ConfiancaClassificacao c) {
        return switch (c) {
            case BAIXA -> 0;
            case MEDIA -> 1;
            case ALTA -> 2;
        };
    }

    private static List<String> aplicarTruncamento(final List<String> justificativas) {
        if (justificativas.size() <= 40) {
            return justificativas;
        }
        return justificativas.subList(0, 40);
    }

    public static Map<CategoriaReclamacao, CriticidadeRegra> maiorCriticidadeAcumulada(
        final Map<CategoriaReclamacao, CriticidadeRegra> acumulado,
        final CategoriaReclamacao categoria,
        final CriticidadeRegra criticidade
    ) {
        final EnumMap<CategoriaReclamacao, CriticidadeRegra> map =
            acumulado == null ? new EnumMap<>(CategoriaReclamacao.class) : new EnumMap<>(acumulado);
        map.merge(categoria, criticidade, PoliticaConfiancaClassificacao::maxCriticidade);
        return map;
    }

    private static CriticidadeRegra maxCriticidade(final CriticidadeRegra a, final CriticidadeRegra b) {
        if (ordinalCrit(a) >= ordinalCrit(b)) {
            return a;
        }
        return b;
    }

    private static int ordinalCrit(final CriticidadeRegra c) {
        return switch (c) {
            case BAIXA -> 0;
            case MEDIA -> 1;
            case ALTA -> 2;
        };
    }
}
