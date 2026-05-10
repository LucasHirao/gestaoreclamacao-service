package com.banco.reclamacoes.domain.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class NormalizacaoTextual {

    private NormalizacaoTextual() {}

    public static String normalizar(final String entrada) {
        if (entrada == null || entrada.isEmpty()) {
            return "";
        }
        final var semAcento = removerAcentos(entrada);
        final var lowercase = semAcento.toLowerCase(Locale.ROOT);
        final var semPont = lowercase.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
        return semPont.trim().replaceAll("\\s+", " ");
    }

    public static List<String> tokenizar(final String textoNormalizado) {
        if (textoNormalizado == null || textoNormalizado.isBlank()) {
            return List.of();
        }
        return new ArrayList<>(Arrays.asList(textoNormalizado.split("\\s+")));
    }

    public static boolean contemTodosTermos(final String textoNormalizado, final List<String> termosNormalizados) {
        for (final var termo : termosNormalizados) {
            if (!textoNormalizado.contains(termo)) {
                return false;
            }
        }
        return true;
    }

    private static String removerAcentos(final String texto) {
        final var normalizada = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizada.replaceAll("\\p{M}+", "");
    }
}
