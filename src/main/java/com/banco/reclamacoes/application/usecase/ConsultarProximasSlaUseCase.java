package com.banco.reclamacoes.application.usecase;

import com.banco.reclamacoes.application.port.output.ParametrosSlaPort;
import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.domain.model.Reclamacao;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsultarProximasSlaUseCase {

    private static final long HORIZONTE_PADRAO_HORAS = 72L;

    private final ReclamacaoRepositoryPort repositorio;
    private final Clock clock;
    private final ParametrosSlaPort parametrosSlaPort;

    public List<Reclamacao> executar(final long horizonteHorasPadrao, final int limite) {
        final Instant limiteTemporal = horizonteBusca(horizonteHorasPadrao);
        return repositorio.buscarProximasAoVencimento(limiteTemporal, limite);
    }

    public List<Reclamacao> executarPadrao() {
        final int limite = parametrosSlaPort.carregar().limiteBuscaAlertas();
        return executar(HORIZONTE_PADRAO_HORAS, limite);
    }

    private Instant horizonteBusca(final long horizonteHorasSolicitado) {
        final long horasEfetivas =
                horizonteHorasSolicitado <= 0 ? HORIZONTE_PADRAO_HORAS : horizonteHorasSolicitado;
        return clock.instant().plus(horasEfetivas, ChronoUnit.HOURS);
    }
}
