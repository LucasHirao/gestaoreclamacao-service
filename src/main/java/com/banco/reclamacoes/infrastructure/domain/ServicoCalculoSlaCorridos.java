package com.banco.reclamacoes.infrastructure.domain;

import com.banco.reclamacoes.domain.port.ServicoCalculoSla;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.PrazoSla;
import com.banco.reclamacoes.domain.valueobject.Sla;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class ServicoCalculoSlaCorridos implements ServicoCalculoSla {

    @Override
    public Sla calcular(final Instant dataRecebimento, final ParametrosSla parametros) {
        Instant deadlineAt = dataRecebimento.plus(parametros.prazoPadraoDiasCorridos(), ChronoUnit.DAYS);
        Instant alertAt = deadlineAt.minus(parametros.diasAntesParaAlerta(), ChronoUnit.DAYS);
        if (alertAt.isBefore(dataRecebimento)) {
            alertAt = dataRecebimento;
        }
        return Sla.of(PrazoSla.de(deadlineAt), PrazoSla.de(alertAt));
    }
}
