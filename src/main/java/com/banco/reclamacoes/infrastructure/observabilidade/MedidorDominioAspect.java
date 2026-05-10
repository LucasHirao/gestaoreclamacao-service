package com.banco.reclamacoes.infrastructure.observabilidade;

import com.banco.reclamacoes.application.port.output.IntegracaoPublicacaoComando;
import com.banco.reclamacoes.application.port.output.MedidorDominioPort;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Métricas Micrometer delegadas a aspectos para manter casos de uso focados em orquestração.
 */
@Aspect
@Component
@Order(1)
public class MedidorDominioAspect {

    private final MedidorDominioPort medidor;

    public MedidorDominioAspect(final MedidorDominioPort medidor) {
        this.medidor = medidor;
    }

    @Around(
            "execution(* com.banco.reclamacoes.domain.port.MotorClassificacaoPort.classificar(..))")
    public Object medirTempoClassificacao(final ProceedingJoinPoint joinPoint) throws Throwable {
        final long inicio = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            medidor.registrarTempoClassificacaoNanos(System.nanoTime() - inicio);
        }
    }

    @AfterReturning(
            pointcut =
                    "execution(* com.banco.reclamacoes.domain.port.MotorClassificacaoPort.classificar(..))",
            returning = "resultado")
    public void medirClassificacaoBaixaConfianca(final ResultadoClassificacao resultado) {
        if (ConfiancaClassificacao.BAIXA.equals(resultado.confianca())) {
            medidor.registrarClassificacaoBaixaConfianca();
        }
    }

    @Before(
            "execution(* com.banco.reclamacoes.application.port.output.ProcessamentoSolicitacaoMarcadoresPort"
                    + ".aposPrimeiraPersistencia(..))")
    public void medirCriacaoReclamacao() {
        medidor.registrarCriacaoReclamacao();
    }

    @AfterReturning(
            "execution(* com.banco.reclamacoes.application.port.output.PublicadorIntegracaoPort.publicar(..)) && "
                    + "args(comando)")
    public void medirIntegracaoPublicada(final IntegracaoPublicacaoComando comando) {
        medidor.registrarIntegracaoPublicada(comando.categoria());
    }

    @AfterThrowing(
            pointcut =
                    "execution(* com.banco.reclamacoes.application.usecase.ProcessarSolicitacaoPadronizadaUseCase"
                            + ".processar(..))",
            throwing = "erro")
    public void medirErroProcessamento(final Throwable erro) {
        medidor.registrarErroProcessamento();
    }

    @AfterReturning(
            "execution(* com.banco.reclamacoes.application.port.output.NotificacaoPort.notificarAlertaSla(..))")
    public void medirAlertaSlaEmitido() {
        medidor.registrarAlertaSlaEmitido();
    }
}
