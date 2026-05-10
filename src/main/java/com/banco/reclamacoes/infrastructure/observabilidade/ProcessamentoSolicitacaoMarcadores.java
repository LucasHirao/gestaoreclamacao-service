package com.banco.reclamacoes.infrastructure.observabilidade;

import com.banco.reclamacoes.application.port.output.ProcessamentoSolicitacaoMarcadoresPort;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação do port — métrica de criação ({@link MedidorDominioAspect}) e log de SLA aplicados
 * por programação orientada a aspectos sobre este bean.
 */
@Component
public class ProcessamentoSolicitacaoMarcadores implements ProcessamentoSolicitacaoMarcadoresPort {

    private static final Logger log = LoggerFactory.getLogger(ProcessamentoSolicitacaoMarcadores.class);

    @Override
    public void aposPrimeiraPersistencia(
            final Reclamacao salva, final ResultadoClassificacao resultadoDominio) {
        log.debug(
                "SLA calculado deadline={} alerta={} categoria={}",
                salva.sla().deadline().instant(),
                salva.sla().alerta().instant(),
                resultadoDominio.categoriaPrincipal());
    }
}
