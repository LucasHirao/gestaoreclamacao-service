package com.banco.reclamacoes.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class NormalizacaoTextualTest {

    @Test
    void normalizaNuloEVazio() {
        assertThat(NormalizacaoTextual.normalizar(null)).isEmpty();
        assertThat(NormalizacaoTextual.normalizar("")).isEmpty();
    }

    @Test
    void normalizaRemovePontuacaoEColapsaEspacos() {
        assertThat(NormalizacaoTextual.normalizar("  Olá, MUNDO-123!!  ")).isEqualTo("ola mundo 123");
    }

    @Test
    void tokenizaNuloOuEmBrancoEVazio() {
        assertThat(NormalizacaoTextual.tokenizar(null)).isEmpty();
        assertThat(NormalizacaoTextual.tokenizar("")).isEmpty();
        assertThat(NormalizacaoTextual.tokenizar("   ")).isEmpty();
    }

    @Test
    void tokenizaPalavrasSeparadas() {
        assertThat(NormalizacaoTextual.tokenizar("a bc def")).containsExactly("a", "bc", "def");
    }

    @Test
    void contemTodosTermosExigeCadaTrecho() {
        String t = "cobra fatura cartao";
        assertThat(NormalizacaoTextual.contemTodosTermos(t, List.of("fatura", "cartao"))).isTrue();
        assertThat(NormalizacaoTextual.contemTodosTermos(t, List.of("pix", "cartao"))).isFalse();
    }
}
