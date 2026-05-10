package com.banco.reclamacoes.infrastructure.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.RepeatedTest;

class GeradorProtocoloAleatorioTest {

    private final GeradorProtocoloAleatorio gerador = new GeradorProtocoloAleatorio();

    @RepeatedTest(3)
    void geraProtocoloComPrefixoRecDataESufixoNumerico() {
        var protocolo = gerador.gerarParaDataRecebimento(Instant.parse("2026-07-01T08:00:00Z"));

        assertThat(protocolo.valor()).matches("REC-20260701-\\d{5}");
    }
}
