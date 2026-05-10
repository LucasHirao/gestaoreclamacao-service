package com.banco.reclamacoes.domain.service;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicao;
import com.banco.reclamacoes.domain.model.RegraCompostaDefinicao;
import com.banco.reclamacoes.domain.policy.PoliticaConfiancaClassificacao;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ScoreClassificacao;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Motor de classificação por regras ponderadas (índice invertido, frases, regex e compostas).
 *
 * <p>Instanciado via {@link #montar} com dados já materializados — sem dependência de
 * infraestrutura ou frameworks.</p>
 */
public final class ClassificadorPorRegrasPonderadas {

    private final Map<String, List<RegraClassificacaoDefinicao>> indicePorTermo;
    private final List<RegraClassificacaoDefinicao> regrasFrase;
    private final List<RegraRegexCompilada> regrasRegex;
    private final List<RegraCompostaDefinicao> regrasCompostas;
    private final Map<String, String> sinonimos;

    private ClassificadorPorRegrasPonderadas(
            final Map<String, List<RegraClassificacaoDefinicao>> indicePorTermo,
            final List<RegraClassificacaoDefinicao> regrasFrase,
            final List<RegraRegexCompilada> regrasRegex,
            final List<RegraCompostaDefinicao> regrasCompostas,
            final Map<String, String> sinonimos) {
        this.indicePorTermo = indicePorTermo;
        this.regrasFrase = regrasFrase;
        this.regrasRegex = regrasRegex;
        this.regrasCompostas = regrasCompostas;
        this.sinonimos = sinonimos;
    }

    /**
     * Compila regras e sinónimos em estruturas eficientes para classificação.
     */
    public static ClassificadorPorRegrasPonderadas montar(
            final List<RegraClassificacaoDefinicao> regras,
            final List<RegraCompostaDefinicao> regrasCompostas,
            final Map<String, String> sinonimosVariantesParaCanonico) {
        final var indice = new HashMap<String, List<RegraClassificacaoDefinicao>>();
        final var frases = new ArrayList<RegraClassificacaoDefinicao>();
        final var regex = new ArrayList<RegraRegexCompilada>();
        for (final var regra : regras) {
            switch (regra.tipo()) {
                case PALAVRA -> {
                    if (regra.termoNormalizado().contains(" ")) {
                        frases.add(regra);
                    } else {
                        indice.computeIfAbsent(regra.termoNormalizado(), k -> new ArrayList<>()).add(regra);
                    }
                }
                case FRASE -> frases.add(regra);
                case REGEX ->
                    regex.add(new RegraRegexCompilada(Pattern.compile(regra.termoNormalizado()), regra));
                case COMPOSTA -> {}
            }
        }
        return new ClassificadorPorRegrasPonderadas(
                Map.copyOf(indice),
                List.copyOf(frases),
                List.copyOf(regex),
                List.copyOf(regrasCompostas),
                Map.copyOf(sinonimosVariantesParaCanonico));
    }

    public ResultadoClassificacao classificar(final DescricaoReclamacao descricao) {
        final var texto = NormalizacaoTextual.normalizar(descricao.texto());
        final var acumulacao = new AcumulacaoPontuacaoClassificacao();

        final var tokens = NormalizacaoTextual.tokenizar(texto);
        for (final var token : tokens) {
            final var canonico = sinonimos.getOrDefault(token, token);
            aplicarRegrasDoIndice(token, acumulacao);
            if (!canonico.equals(token)) {
                aplicarRegrasDoIndice(canonico, acumulacao);
            }
        }

        final var frasesAplicadas = new HashSet<String>();
        for (final var regra : regrasFrase) {
            final var chave = chaveFrase(regra);
            if (!texto.contains(regra.termoNormalizado()) || !frasesAplicadas.add(chave)) {
                continue;
            }
            acumulacao.contribuir(regra.categoria(), regra.peso(), regra.justificativa());
        }

        for (final var regra : regrasRegex) {
            if (regra.pattern().matcher(texto).find()) {
                final var def = regra.definicao();
                acumulacao.contribuir(def.categoria(), def.peso(), def.justificativa());
            }
        }

        final var compostasAplicadas = new HashSet<String>();
        for (final var composta : regrasCompostas) {
            final var chave = composta.categoria().name() + "|"
                + String.join("&", composta.termosObrigatoriosNormalizados());
            if (!compostasAplicadas.add(chave)) {
                continue;
            }
            if (NormalizacaoTextual.contemTodosTermos(texto, composta.termosObrigatoriosNormalizados())) {
                acumulacao.contribuir(composta.categoria(), composta.pesoExtra(), composta.justificativa());
            }
        }

        final var pontuacaoDominio = paraDominio(acumulacao.porCategoria());
        final var fraudePontos =
            pontuacaoDominio.getOrDefault(CategoriaReclamacao.FRAUDE, ScoreClassificacao.de(0));

        return PoliticaConfiancaClassificacao.aplicar(pontuacaoDominio, acumulacao.justificativas(), fraudePontos);
    }

    private void aplicarRegrasDoIndice(final String chave, final AcumulacaoPontuacaoClassificacao acumulacao) {
        final var regrasAssociadas =
            indicePorTermo.getOrDefault(chave, List.of());
        for (final var regra : regrasAssociadas) {
            acumulacao.contribuir(regra.categoria(), regra.peso(), regra.justificativa());
        }
    }

    private static Map<CategoriaReclamacao, ScoreClassificacao> paraDominio(
        final EnumMap<CategoriaReclamacao, Integer> acumulador
    ) {
        final var dominio = new EnumMap<CategoriaReclamacao, ScoreClassificacao>(CategoriaReclamacao.class);
        acumulador.forEach((cat, pts) -> dominio.put(cat, ScoreClassificacao.de(pts)));
        return dominio;
    }

    private static String chaveFrase(final RegraClassificacaoDefinicao regra) {
        return regra.categoria().name() + "|" + regra.tipo().name() + "|" + regra.termoNormalizado();
    }
}
