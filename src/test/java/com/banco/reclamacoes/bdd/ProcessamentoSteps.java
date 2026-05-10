package com.banco.reclamacoes.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.banco.reclamacoes.application.command.ProcessarSolicitacaoPadronizadaCommand;
import com.banco.reclamacoes.application.port.input.RegistrarSolicitacaoPadronizadaPort;
import com.banco.reclamacoes.domain.port.MotorClassificacaoPort;
import com.banco.reclamacoes.application.port.output.ParametrosSlaPort;
import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.application.usecase.VerificarSlaUseCase;
import com.banco.reclamacoes.domain.model.CanalOrigem;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.port.ProvedorIdentidadeReclamacao;
import com.banco.reclamacoes.domain.port.ServicoCalculoSla;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.model.ReclamacaoPersistenciaSnapshot;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.Classificacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.Sla;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProcessamentoSteps {

    private final ReclamacaoRepositoryPort repositorio;
    private final RegistrarSolicitacaoPadronizadaPort processarSolicitacaoPort;
    private final MotorClassificacaoPort motorClassificacao;
    private final ParametrosSlaPort parametrosSlaPort;
    private final VerificarSlaUseCase verificarSlaUseCase;
    private final ServicoCalculoSla servicoCalculoSla;
    private final ProvedorIdentidadeReclamacao provedorIdentidade;
    private final Clock clock;
    private final CenarioContexto contexto;

    private ProcessarSolicitacaoPadronizadaCommand ultimoComando;
    private Optional<UUID> ultimoUuidProcessamento = Optional.empty();

    public ProcessamentoSteps(
        final ReclamacaoRepositoryPort repositorio,
        final RegistrarSolicitacaoPadronizadaPort processarSolicitacaoPort,
        final MotorClassificacaoPort motorClassificacao,
        final ParametrosSlaPort parametrosSlaPort,
        final VerificarSlaUseCase verificarSlaUseCase,
        final ServicoCalculoSla servicoCalculoSla,
        final ProvedorIdentidadeReclamacao provedorIdentidade,
        final Clock clock,
        final CenarioContexto contexto) {
        this.repositorio = repositorio;
        this.processarSolicitacaoPort = processarSolicitacaoPort;
        this.motorClassificacao = motorClassificacao;
        this.parametrosSlaPort = parametrosSlaPort;
        this.verificarSlaUseCase = verificarSlaUseCase;
        this.servicoCalculoSla = servicoCalculoSla;
        this.provedorIdentidade = provedorIdentidade;
        this.clock = clock;
        this.contexto = contexto;
    }

    @Before
    public void limparEstadoDoCenario() {
        ultimoComando = null;
        ultimoUuidProcessamento = Optional.empty();
    }

    @Given("que existe uma solicitação padronizada válida")
    public void solicitacaoPadronizadaValida() {
        ultimoComando =
            comandoBase(
                "SOL-BDD-%s".formatted(UUID.randomUUID()),
                "idem-%s".formatted(UUID.randomUUID()),
                "Cliente relata problema sério com tarifas e extrato que não batem com o uso real.");
    }

    @When("a solicitação for processada")
    public void processarSolicitacao() {
        ultimoUuidProcessamento = processarSolicitacaoPort.processar(ultimoComando);
        ultimoUuidProcessamento.ifPresent(
            uuid ->
                repositorio
                    .buscarPorId(ReclamacaoId.de(uuid))
                    .ifPresent(r -> contexto.registrarUltimaReclamacao(uuid, r.protocolo().valor())));
    }

    @Then("uma reclamação oficial deve ser criada")
    public void reclamacaoOficialCriada() {
        assertThat(contexto.ultimaReclamacaoId()).isNotNull();
    }

    @Then("deve possuir protocolo")
    public void devePossuirProtocolo() {
        assertThat(contexto.ultimoProtocolo()).isNotBlank();
    }

    @Then("deve possuir classificação")
    public void devePossuirClassificacao() {
        Optional<Reclamacao> r = buscarUltima();
        assertThat(r).isPresent();
        assertThat(r.get().resultadoClassificacao()).isNotNull();
    }

    @Then("deve possuir SLA")
    public void devePossuirSla() {
        Optional<Reclamacao> r = buscarUltima();
        assertThat(r).isPresent();
        assertThat(r.get().sla()).isNotNull();
    }

    @Given("que uma solicitação padronizada já foi processada")
    public void jaProcessada() {
        solicitacaoPadronizadaValida();
        processarSolicitacao();
        assertThat(contexto.ultimaReclamacaoId()).isNotNull();
    }

    @When("a mesma solicitação for processada novamente")
    public void reprocessarMesmaSolicitacao() {
        ultimoUuidProcessamento = processarSolicitacaoPort.processar(ultimoComando);
    }

    @Then("nenhuma nova reclamação deve ser criada")
    public void semNovaReclamacao() {
        assertThat(ultimoUuidProcessamento).contains(contexto.ultimaReclamacaoId());
    }

    @Given("uma solicitação com texto {string}")
    public void solicitarComTexto(final String texto) {
        ultimoComando =
            comandoBase(
                "SOL-FRAUDE-%s".formatted(UUID.randomUUID()),
                "idem-fraud-%s".formatted(UUID.randomUUID()),
                texto);
    }

    @Then("a categoria principal deve ser FRAUDE")
    public void categoriaFraude() {
        Optional<Reclamacao> r = buscarUltima();
        assertThat(r).isPresent();
        assertThat(r.get().classificacao().categoriaPrincipal()).isEqualTo(CategoriaReclamacao.FRAUDE);
    }

    @Then("a justificativa deve conter regra relacionada a compra não reconhecida")
    public void justificativaCompra() {
        Optional<Reclamacao> r = buscarUltima();
        assertThat(r).isPresent();
        List<String> justificativas = r.get().resultadoClassificacao().justificativas();
        assertThat(
                justificativas.stream()
                    .anyMatch(
                        j ->
                            j.toLowerCase().contains("composta")
                                || j.toLowerCase().contains("compra")
                                || j.toLowerCase().contains("reconhecida")))
            .isTrue();
    }

    @Given("uma solicitação com texto ambíguo entre cobrança e cartão")
    public void ambigua() {
        ultimoComando =
            comandoBase(
                "SOL-AMB-%s".formatted(UUID.randomUUID()),
                "idem-amb-%s".formatted(UUID.randomUUID()),
                "Tive cobrança estranha na minha fatura mas também uso muito o cartão nos últimos dias.");
    }

    @Then("a classificação deve indicar confiança baixa ou média")
    public void confiancaBaixaOuMedia() {
        Optional<Reclamacao> r = buscarUltima();
        assertThat(r).isPresent();
        ConfiancaClassificacao conf = r.get().classificacao().confianca();
        assertThat(conf == ConfiancaClassificacao.BAIXA || conf == ConfiancaClassificacao.MEDIA).isTrue();
    }

    @Then("deve conter categorias candidatas")
    public void candidatas() {
        Optional<Reclamacao> r = buscarUltima();
        assertThat(r).isPresent();
        assertThat(r.get().resultadoClassificacao().categoriasCandidatas().size()).isGreaterThan(1);
    }

    @Given("uma reclamação com alertAt vencido")
    public void reclamacaoComAlertaVencido() {
        Instant agora = Instant.now(clock);
        Instant recebimento = agora.minus(20, ChronoUnit.DAYS);
        ParametrosSla p = parametrosSlaPort.carregar();

        DescricaoReclamacao descricao =
            DescricaoReclamacao.de(
                "Relato detalhado sobre saldo e extrato da conta corrente com mais de dez caracteres.");

        ResultadoClassificacao resultadoClassificacao = motorClassificacao.classificar(descricao);
        Classificacao classificacao =
            Classificacao.de(resultadoClassificacao.categoriaPrincipal(), resultadoClassificacao.confianca());

        Sla sla = servicoCalculoSla.calcular(recebimento, p);

        final var snapshot =
            ReclamacaoPersistenciaSnapshot.builder()
                .id(provedorIdentidade.novaIdentidade())
                .solicitacaoId(SolicitacaoId.de("SOL-SLA-%s".formatted(UUID.randomUUID())))
                .protocolo(
                    Protocolo.de("PROT-SLA-%s".formatted(UUID.randomUUID().toString().substring(0, 8))))
                .clienteId(ClienteId.de("CLI-SLA"))
                .canalOrigem(CanalOrigem.PORTAL_INTERNO)
                .descricao(descricao)
                .status(StatusReclamacao.ABERTA)
                .classificacao(classificacao)
                .resultadoClassificacao(resultadoClassificacao)
                .dataRecebimento(recebimento)
                .sla(sla)
                .alertaSlaEmitido(false)
                .integracaoRegistrada(false)
                .correlationId(CorrelationId.de("COR-SLA"))
                .idempotencyKey(IdempotencyKey.de("idem-sla-%s".formatted(UUID.randomUUID())))
                .anexos(List.of())
                .historicosPersistidos(List.of())
                .build();
        final var reclamacao = Reclamacao.restaurarPersistida(snapshot);

        repositorio.salvar(reclamacao);
        contexto.registrarUltimaReclamacao(reclamacao.id().valor(), reclamacao.protocolo().valor());
    }

    @When("o job de SLA for executado")
    public void executarJobSla() {
        verificarSlaUseCase.executar();
    }

    @Then("um alerta deve ser emitido")
    public void alertaEmitido() {
        Optional<Reclamacao> r = buscarUltima();
        assertThat(r).isPresent();
        assertThat(r.get().alertaSlaEmitido()).isTrue();
    }

    @Then("a reclamação deve registrar alerta emitido")
    public void registrarAlerta() {
        alertaEmitido();
    }

    private Optional<Reclamacao> buscarUltima() {
        if (contexto.ultimaReclamacaoId() == null) {
            return Optional.empty();
        }
        return repositorio.buscarPorId(ReclamacaoId.de(contexto.ultimaReclamacaoId()));
    }

    private ProcessarSolicitacaoPadronizadaCommand comandoBase(
        final String solicitacaoId,
        final String idempotencyKey,
        final String descricao) {
        return new ProcessarSolicitacaoPadronizadaCommand(
            "1",
            solicitacaoId,
            "CLI-BDD",
            CanalOrigem.DIGITAL,
            descricao,
            List.of(),
            Instant.now(clock),
            "COR-BDD-" + UUID.randomUUID(),
            idempotencyKey,
            java.util.Map.of());
    }
}
