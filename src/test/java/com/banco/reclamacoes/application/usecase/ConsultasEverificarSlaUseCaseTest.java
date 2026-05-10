package com.banco.reclamacoes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banco.reclamacoes.application.port.output.NotificacaoPort;
import com.banco.reclamacoes.application.port.output.ParametrosSlaPort;
import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.application.query.BuscarReclamacaoPorProtocoloQuery;
import com.banco.reclamacoes.application.query.BuscarReclamacoesFiltrosQuery;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultasEverificarSlaUseCaseTest {

    @Mock
    private ReclamacaoRepositoryPort repositorio;

    @Mock
    private NotificacaoPort notificacaoPort;

    @Mock
    private ParametrosSlaPort parametrosSlaPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-10T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void consultarPorProtocoloDelegaRepositorio() {
        var caso = new ConsultarReclamacaoUseCase(repositorio);
        when(repositorio.buscarPorProtocolo(any())).thenReturn(Optional.empty());

        caso.porProtocolo(new BuscarReclamacaoPorProtocoloQuery("PROT-X"));

        verify(repositorio).buscarPorProtocolo(Protocolo.de("PROT-X"));
    }

    @Test
    void consultarListarPassaFiltros() {
        var caso = new ConsultarReclamacaoUseCase(repositorio);
        when(repositorio.buscar(any(), any(), any(), eq(0), eq(20))).thenReturn(List.of());

        caso.listar(
            new BuscarReclamacoesFiltrosQuery("CLI-1", StatusReclamacao.ABERTA, null, 0, 20));

        verify(repositorio)
            .buscar(eq(ClienteId.de("CLI-1")), eq(StatusReclamacao.ABERTA), eq(null), eq(0), eq(20));
    }

    @Test
    void consultarProximasSlaUsaHorizontePadraoQuandoInvalido() {
        var caso = new ConsultarProximasSlaUseCase(repositorio, clock, parametrosSlaPort);
        when(repositorio.buscarProximasAoVencimento(any(), eq(5))).thenReturn(List.of());

        caso.executar(0L, 5);

        ArgumentCaptor<Instant> limite = ArgumentCaptor.forClass(Instant.class);
        verify(repositorio).buscarProximasAoVencimento(limite.capture(), eq(5));
        assertThat(limite.getValue()).isEqualTo(Instant.parse("2026-05-13T12:00:00Z"));
    }

    @Test
    void consultarProximasSlaPadraoUsaParametros() {
        var caso = new ConsultarProximasSlaUseCase(repositorio, clock, parametrosSlaPort);
        when(parametrosSlaPort.carregar()).thenReturn(ParametrosSla.of(10, 2, 15));
        when(repositorio.buscarProximasAoVencimento(any(), eq(15))).thenReturn(List.of());

        caso.executarPadrao();

        verify(repositorio).buscarProximasAoVencimento(any(), eq(15));
    }

    @Test
    void verificarSlaNotificaPersisteEMede() {
        var caso = new VerificarSlaUseCase(repositorio, notificacaoPort, parametrosSlaPort, clock);
        when(parametrosSlaPort.carregar()).thenReturn(ParametrosSla.of(10, 2, 10));
        Reclamacao r = org.mockito.Mockito.mock(Reclamacao.class);
        when(r.id()).thenReturn(ReclamacaoId.gerar());
        when(r.protocolo()).thenReturn(Protocolo.de("PROT-SLA-UT"));
        when(repositorio.buscarReclamacoesComAlertaPendente(any(), eq(10))).thenReturn(List.of(r));

        int n = caso.executar();

        assertThat(n).isEqualTo(1);
        verify(notificacaoPort).notificarAlertaSla(any(), any(), any());
        verify(r).marcarAlertaSlaEmitido(clock);
        verify(repositorio).salvar(r);
    }
}
