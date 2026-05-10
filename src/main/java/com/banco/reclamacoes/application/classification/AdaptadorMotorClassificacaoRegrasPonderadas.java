package com.banco.reclamacoes.application.classification;

import com.banco.reclamacoes.domain.port.MotorClassificacaoPort;
import com.banco.reclamacoes.application.port.output.RegrasClassificacaoPort;
import com.banco.reclamacoes.application.port.output.RegrasCompostasPort;
import com.banco.reclamacoes.application.port.output.SinonimosClassificacaoPort;
import com.banco.reclamacoes.domain.service.ClassificadorPorRegrasPonderadas;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Liga o carregamento de regras (ports / JSON) ao {@link ClassificadorPorRegrasPonderadas} de domínio.
 */
@Component
@DependsOn({"jsonSinonimosClassificacaoRepository", "jsonRegrasClassificacaoRepository"})
@RequiredArgsConstructor
public class AdaptadorMotorClassificacaoRegrasPonderadas implements MotorClassificacaoPort {

    private final SinonimosClassificacaoPort sinonimosPort;
    private final RegrasClassificacaoPort regrasPort;
    private final RegrasCompostasPort compostasPort;

    private ClassificadorPorRegrasPonderadas motor;

    @PostConstruct
    void prepararMotor() {
        motor = ClassificadorPorRegrasPonderadas.montar(
                regrasPort.regras(),
                compostasPort.regrasCompostas(),
                sinonimosPort.variantesNormalizadasParaCanonico());
    }

    @Override
    public ResultadoClassificacao classificar(final DescricaoReclamacao descricao) {
        return motor.classificar(descricao);
    }
}
