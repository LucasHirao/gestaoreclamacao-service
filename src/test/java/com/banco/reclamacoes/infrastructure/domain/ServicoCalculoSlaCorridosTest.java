package com.banco.reclamacoes.infrastructure.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.Sla;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class ServicoCalculoSlaCorridosTest {

    private final ServicoCalculoSlaCorridos servico = new ServicoCalculoSlaCorridos();

    @Test
    void calculaDeadlineEAlertaConformeParametrosExternos() {
        ParametrosSla parametros = ParametrosSla.of(10, 2, 100);
        Instant recebimento = Instant.parse("2026-03-01T10:00:00Z");

        Sla sla = servico.calcular(recebimento, parametros);

        Instant esperadoDeadline = recebimento.plus(10, ChronoUnit.DAYS);
        Instant esperadoAlert = esperadoDeadline.minus(2, ChronoUnit.DAYS);
        assertThat(sla.deadline().instant()).isEqualTo(esperadoDeadline);
        assertThat(sla.alerta().instant()).isEqualTo(esperadoAlert);
    }

    @Test
    void naoRetrocedeAlertaAntesDoRecebimento() {
        ParametrosSla parametros = ParametrosSla.of(1, 5, 10);
        Instant recebimento = Instant.parse("2026-06-01T12:00:00Z");

        Sla sla = servico.calcular(recebimento, parametros);

        assertThat(sla.alerta().instant()).isAfterOrEqualTo(recebimento);
    }
}
