package com.banco.reclamacoes.infrastructure.messaging;

import com.banco.reclamacoes.application.usecase.VerificarSlaUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Entrada típica em produção: agendamento no EventBridge envia mensagem para esta fila SQS,
 * disparando uma execução de {@link VerificarSlaUseCase}. O corpo pode ser JSON do próprio EB;
 * apenas o recebimento da mensagem causa o ciclo — sem interpretar payload.
 */
@Component
@Profile("!test")
@ConditionalOnExpression("'${aws.sqs.sla-verification-queue:}' != ''")
public class VerificacaoSlaSqsSubscriber {

    private static final Logger log = LoggerFactory.getLogger(VerificacaoSlaSqsSubscriber.class);

    private final VerificarSlaUseCase verificarSlaUseCase;

    VerificacaoSlaSqsSubscriber(final VerificarSlaUseCase verificarSlaUseCase) {
        this.verificarSlaUseCase = verificarSlaUseCase;
    }

    @SqsListener("${aws.sqs.sla-verification-queue}")
    public void aoReceberDisparoVerificacaoSla(final String corpo) {
        try {
            final var processadas = verificarSlaUseCase.executar();
            if (log.isDebugEnabled()) {
                log.debug("Payload disparo SLA: {} caracteres", corpo != null ? corpo.length() : 0);
            }
            if (processadas > 0) {
                log.info("Verificação SLA (SQS) processou {} reclamações com alertas pendentes", processadas);
            }
        } catch (final Exception e) {
            log.error("Erro ao executar verificação SLA após mensagem da fila", e);
            throw e;
        }
    }
}
