package com.banco.reclamacoes.infrastructure.observabilidade;

import com.banco.reclamacoes.application.command.ProcessarSolicitacaoPadronizadaCommand;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Propaga correlação da solicitação ao MDC para logs correlacionados durante o processamento.
 */
@Aspect
@Component
@Order(0)
public class MdcProcessamentoSolicitacaoPadronizadaAspect {

    @Around(
            "execution(* com.banco.reclamacoes.application.usecase.ProcessarSolicitacaoPadronizadaUseCase"
                    + ".processar(..)) && args(command)")
    public Object comMdc(
            final ProceedingJoinPoint joinPoint, final ProcessarSolicitacaoPadronizadaCommand command)
            throws Throwable {
        MDC.put("correlationId", command.correlationId());
        MDC.put("solicitacaoId", command.solicitacaoId());
        try {
            return joinPoint.proceed();
        } finally {
            MDC.remove("correlationId");
            MDC.remove("solicitacaoId");
        }
    }
}
