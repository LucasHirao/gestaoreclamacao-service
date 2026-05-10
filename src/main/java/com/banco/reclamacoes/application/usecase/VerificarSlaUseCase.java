package com.banco.reclamacoes.application.usecase;

import com.banco.reclamacoes.application.port.output.NotificacaoPort;
import com.banco.reclamacoes.application.port.output.ParametrosSlaPort;
import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificarSlaUseCase {

    private static final Logger log = LoggerFactory.getLogger(VerificarSlaUseCase.class);

    private final ReclamacaoRepositoryPort repositorio;
    private final NotificacaoPort notificacaoPort;
    private final ParametrosSlaPort parametrosSlaPort;
    private final Clock clock;

    /**
     * Notifica e persiste alertas de SLA para reclamações com janela de alerta vencida e alerta ainda não emitido.
     *
     * @return quantidade de reclamações efetivamente processadas nesta execução
     */
    @Transactional
    public int executar() {
        final ParametrosSla parametros = parametrosSlaPort.carregar();
        final Instant agora = Instant.now(clock);
        final List<Reclamacao> candidatas =
                repositorio.buscarReclamacoesComAlertaPendente(agora, parametros.limiteBuscaAlertas());

        int processadas = 0;
        for (final Reclamacao reclamacao : candidatas) {
            emitirAlertaSalvar(reclamacao, agora);
            processadas++;
        }
        return processadas;
    }

    private void emitirAlertaSalvar(final Reclamacao reclamacao, final Instant referenciaTemporal) {
        notificacaoPort.notificarAlertaSla(reclamacao.id(), reclamacao.protocolo(), referenciaTemporal);
        reclamacao.marcarAlertaSlaEmitido(clock);
        repositorio.salvar(reclamacao);
        log.info("Alerta SLA emitido protocolo={}", reclamacao.protocolo().valor());
    }
}
