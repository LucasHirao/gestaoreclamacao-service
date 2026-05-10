package com.banco.reclamacoes.infrastructure.observabilidade;

import com.banco.reclamacoes.application.port.output.MedidorDominioPort;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class MedidorMicrometerAdapter implements MedidorDominioPort {

    private final MeterRegistry registry;
    private final Counter reclamacoesCriadas;
    private final Counter classificacaoBaixa;
    private final Counter alertasSla;
    private final Counter errosProcessamento;
    private final Timer timerClassificacao;

    public MedidorMicrometerAdapter(final MeterRegistry registry) {
        this.registry = registry;
        this.reclamacoesCriadas = registry.counter("reclamacoes.criadas");
        this.classificacaoBaixa = registry.counter("reclamacoes.classificacao.confianca.baixa");
        this.alertasSla = registry.counter("reclamacoes.sla.alertas_emitidos");
        this.errosProcessamento = registry.counter("reclamacoes.processamento.erro");
        this.timerClassificacao = registry.timer("reclamacoes.classificacao.tempo");
    }

    @Override
    public void registrarCriacaoReclamacao() {
        reclamacoesCriadas.increment();
    }

    @Override
    public void registrarClassificacaoBaixaConfianca() {
        classificacaoBaixa.increment();
    }

    @Override
    public void registrarTempoClassificacaoNanos(final long nanos) {
        timerClassificacao.record(nanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void registrarAlertaSlaEmitido() {
        alertasSla.increment();
    }

    @Override
    public void registrarIntegracaoPublicada(final CategoriaReclamacao categoria) {
        registry.counter("reclamacoes.integracao.publicada", "categoria", categoria.name()).increment();
    }

    @Override
    public void registrarErroProcessamento() {
        errosProcessamento.increment();
    }
}
