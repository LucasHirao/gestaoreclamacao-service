package com.banco.reclamacoes.domain.service;

import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicao;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Autómato de Aho–Corasick para casar todas as frases normalizadas (literais) num único percorrimento do texto.
 */
final class AutomatoFrasesAhoCorasick {

    private static final AutomatoFrasesAhoCorasick VAZIO = new AutomatoFrasesAhoCorasick(new No());

    private final No raiz;

    private AutomatoFrasesAhoCorasick(final No raiz) {
        this.raiz = raiz;
    }

    static AutomatoFrasesAhoCorasick construir(final List<RegraClassificacaoDefinicao> regrasFrase) {
        if (regrasFrase.isEmpty()) {
            return VAZIO;
        }
        final var novaRaiz = new No();
        for (final var regra : regrasFrase) {
            final var padrao = regra.termoNormalizado();
            if (padrao.isEmpty()) {
                continue;
            }
            inserir(novaRaiz, padrao, regra);
        }
        instalarFalhas(novaRaiz);
        return new AutomatoFrasesAhoCorasick(novaRaiz);
    }

    /**
     * Emite cada ocorrência de casamento: a mesma regra pode ser notificada várias vezes se o padrão repetir no
     * texto — o chamador deduplica se precisar.
     */
    void encontrarLitereais(final String texto, final Consumer<RegraClassificacaoDefinicao> porOcorrencia) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        No estado = raiz;
        for (int i = 0; i < texto.length(); i++) {
            final char c = texto.charAt(i);
            estado = avancar(estado, c);
            reportar(estado, porOcorrencia);
        }
    }

    private static void inserir(final No raiz, final CharSequence padrao, final RegraClassificacaoDefinicao regra) {
        No atual = raiz;
        for (int i = 0; i < padrao.length(); i++) {
            final char c = padrao.charAt(i);
            atual = atual.transicoes.computeIfAbsent(c, ignored -> new No());
        }
        atual.regrasNoFim.add(regra);
    }

    private static void instalarFalhas(final No raiz) {
        final Queue<No> fila = new ArrayDeque<>();
        raiz.falha = null;
        for (final var filho : raiz.transicoes.values()) {
            filho.falha = raiz;
            fila.add(filho);
        }
        while (!fila.isEmpty()) {
            final No atual = fila.remove();
            for (final var entrada : atual.transicoes.entrySet()) {
                final char c = entrada.getKey();
                final No filho = entrada.getValue();
                fila.add(filho);
                No falhaCand = atual.falha;
                while (falhaCand != null && !falhaCand.transicoes.containsKey(c)) {
                    falhaCand = falhaCand.falha;
                }
                filho.falha = falhaCand == null ? raiz : falhaCand.transicoes.get(c);
            }
        }
    }

    private No avancar(No estado, final char c) {
        while (estado != raiz && !estado.transicoes.containsKey(c)) {
            estado = estado.falha;
        }
        final No seguinte = estado.transicoes.get(c);
        return seguinte != null ? seguinte : raiz;
    }

    private static void reportar(final No estado, final Consumer<RegraClassificacaoDefinicao> consumidor) {
        for (No n = estado; n != null; n = n.falha) {
            for (final var regra : n.regrasNoFim) {
                consumidor.accept(regra);
            }
        }
    }

    private static final class No {
        private final Map<Character, No> transicoes = new HashMap<>();
        private No falha;
        private final List<RegraClassificacaoDefinicao> regrasNoFim = new ArrayList<>();
    }
}
