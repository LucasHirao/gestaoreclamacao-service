package com.banco.reclamacoes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banco.reclamacoes.application.command.ProcessarSolicitacaoPadronizadaCommand;
import com.banco.reclamacoes.application.command.AnexoSolicitacaoCommand;
import com.banco.reclamacoes.application.port.output.HistoricoClientePort;
import com.banco.reclamacoes.application.port.MapeadorAnexosSolicitacao;
import com.banco.reclamacoes.application.port.output.ProcessamentoSolicitacaoMarcadoresPort;
import com.banco.reclamacoes.domain.port.MotorClassificacaoPort;
import com.banco.reclamacoes.application.port.output.ParametrosSlaPort;
import com.banco.reclamacoes.application.port.output.PublicadorIntegracaoPort;
import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.CanalOrigem;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.application.port.output.IntegracaoPublicacaoComando;
import com.banco.reclamacoes.domain.model.FabricaReclamacao;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.model.ReclamacaoPersistenciaSnapshot;
import com.banco.reclamacoes.domain.valueobject.CategoriaPontuacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.MontagemResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ScoreClassificacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import com.banco.reclamacoes.infrastructure.application.MapeadorAnexosSolicitacaoPadrao;
import com.banco.reclamacoes.infrastructure.domain.GeradorProtocoloAleatorio;
import com.banco.reclamacoes.infrastructure.domain.ProvedorIdentidadeReclamacaoUuid;
import com.banco.reclamacoes.infrastructure.domain.ServicoCalculoSlaCorridos;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessarSolicitacaoPadronizadaUseCaseTest {

    @Mock
    private ReclamacaoRepositoryPort repositorio;

    @Mock
    private MotorClassificacaoPort motor;

    @Mock
    private ParametrosSlaPort parametrosSlaPort;

    @Mock
    private PublicadorIntegracaoPort publicador;

    @Mock
    private HistoricoClientePort historicoClientePort;

    @Mock
    private ProcessamentoSolicitacaoMarcadoresPort marcadores;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneOffset.UTC);

    private final FabricaReclamacao fabricaReclamacao =
            new FabricaReclamacao(
                    new ProvedorIdentidadeReclamacaoUuid(),
                    new GeradorProtocoloAleatorio(),
                    new ServicoCalculoSlaCorridos());
    private final MapeadorAnexosSolicitacao mapeadorAnexos = new MapeadorAnexosSolicitacaoPadrao();

    private ProcessarSolicitacaoPadronizadaUseCase casoDeUso;

    @BeforeEach
    void inicializar() {
        casoDeUso =
            new ProcessarSolicitacaoPadronizadaUseCase(
                    repositorio,
                    motor,
                    parametrosSlaPort,
                    publicador,
                    historicoClientePort,
                    marcadores,
                    clock,
                    fabricaReclamacao,
                    mapeadorAnexos);
        when(parametrosSlaPort.carregar()).thenReturn(ParametrosSla.of(10, 2, 100));
        lenient().when(repositorio.salvar(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void processaNovaSolicitacaoPadronizadaFluxoFeliz() {
        when(repositorio.buscarPorSolicitacaoId(any())).thenReturn(Optional.empty());
        when(repositorio.buscarPorIdempotencyKey(any())).thenReturn(Optional.empty());
        when(motor.classificar(any()))
            .thenReturn(
                ResultadoClassificacao.montar(
                    MontagemResultadoClassificacao.builder()
                        .categoriaPrincipal(CategoriaReclamacao.COBRANCA)
                        .categoriasCandidatas(
                            List.of(CategoriaPontuacao.de(CategoriaReclamacao.COBRANCA, ScoreClassificacao.de(25))))
                        .scorePrincipal(ScoreClassificacao.de(25))
                        .confianca(ConfiancaClassificacao.ALTA)
                        .justificativas(List.of())
                        .requerRevisaoManual(false)
                        .build()));

        var comando =
            new ProcessarSolicitacaoPadronizadaCommand(
                "1",
                "SOL-UNIT-01",
                "CLI-UNIT",
                CanalOrigem.DIGITAL,
                "Relato suficientemente longo descrevendo cobrança indevida.",
                List.of(),
                Instant.parse("2026-05-10T09:30:00Z"),
                "COR-UNIT",
                "idem-unit-01",
                Map.of());

        Optional<UUID> resultado = casoDeUso.processar(comando);

        assertThat(resultado).isPresent();
        verify(repositorio, org.mockito.Mockito.times(2)).salvar(any(Reclamacao.class));
        verify(publicador, org.mockito.Mockito.times(1)).publicar(any(IntegracaoPublicacaoComando.class));
    }

    @Test
    void naoDuplicaReclamacaoQuandoSolicitacaoJaProcessada() {
        UUID uuidExistente = UUID.randomUUID();
        var solicitacao = SolicitacaoId.de("SOL-IDEM");
        final var existente =
            Reclamacao.restaurarPersistida(
                ReclamacaoPersistenciaSnapshot.builder()
                    .id(ReclamacaoId.de(uuidExistente))
                    .solicitacaoId(solicitacao)
                    .protocolo(Protocolo.de("PROT-OLD"))
                    .clienteId(ClienteId.de("CLI"))
                    .canalOrigem(CanalOrigem.DIGITAL)
                    .descricao(
                        DescricaoReclamacao.de(
                            "Texto suficientemente longo descrevendo um problema já registrado antes."))
                    .status(StatusReclamacao.ABERTA)
                    .classificacao(null)
                    .resultadoClassificacao(null)
                    .dataRecebimento(Instant.parse("2026-05-09T09:00:00Z"))
                    .sla(null)
                    .alertaSlaEmitido(false)
                    .integracaoRegistrada(false)
                    .correlationId(CorrelationId.de("COR"))
                    .idempotencyKey(IdempotencyKey.de("idem"))
                    .anexos(List.of())
                    .historicosPersistidos(List.of())
                    .build());

        when(repositorio.buscarPorSolicitacaoId(any())).thenReturn(Optional.of(existente));

        var comando =
            new ProcessarSolicitacaoPadronizadaCommand(
                "1",
                solicitacao.valor(),
                existente.clienteId().valor(),
                CanalOrigem.DIGITAL,
                existente.descricao().texto(),
                List.of(),
                Instant.now(),
                "COR-2",
                "idem-diff",
                Map.of());

        Optional<UUID> resultado = casoDeUso.processar(comando);

        assertThat(resultado).contains(uuidExistente);
        verify(repositorio, org.mockito.Mockito.never()).salvar(any(Reclamacao.class));
        verify(motor, org.mockito.Mockito.never()).classificar(any());
        verify(publicador, org.mockito.Mockito.never()).publicar(any(IntegracaoPublicacaoComando.class));
    }

    @Test
    void mapeiaListaDeAnexosComAtributosOuNull() {
        when(repositorio.buscarPorSolicitacaoId(any())).thenReturn(Optional.empty());
        when(repositorio.buscarPorIdempotencyKey(any())).thenReturn(Optional.empty());
        when(motor.classificar(any()))
            .thenReturn(
                ResultadoClassificacao.montar(
                    MontagemResultadoClassificacao.builder()
                        .categoriaPrincipal(CategoriaReclamacao.COBRANCA)
                        .categoriasCandidatas(
                            List.of(CategoriaPontuacao.de(CategoriaReclamacao.COBRANCA, ScoreClassificacao.de(20))))
                        .scorePrincipal(ScoreClassificacao.de(20))
                        .confianca(ConfiancaClassificacao.ALTA)
                        .justificativas(List.of())
                        .requerRevisaoManual(false)
                        .build()));

        var comando =
            new ProcessarSolicitacaoPadronizadaCommand(
                "1",
                "SOL-ANEXO",
                "CLI-UNIT",
                CanalOrigem.DIGITAL,
                "Relato suficientemente longo descrevendo cobrança indevida com anexos enviados.",
                List.of(
                    new AnexoSolicitacaoCommand("ref-a", "image/png", Map.of("pagina", "1")),
                    new AnexoSolicitacaoCommand("ref-b", "application/pdf", null)),
                Instant.parse("2026-05-10T09:30:00Z"),
                "COR-ANEXO",
                "idem-anexo-01",
                Map.of());

        Optional<UUID> resultado = casoDeUso.processar(comando);

        assertThat(resultado).isPresent();
        verify(repositorio, org.mockito.Mockito.times(2)).salvar(any(Reclamacao.class));
    }
}
