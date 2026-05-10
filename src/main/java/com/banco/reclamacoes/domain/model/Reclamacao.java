package com.banco.reclamacoes.domain.model;

import com.banco.reclamacoes.domain.event.AlertaSlaEmitido;
import com.banco.reclamacoes.domain.event.EventoDominio;
import com.banco.reclamacoes.domain.event.IntegracaoSolicitada;
import com.banco.reclamacoes.domain.event.ReclamacaoClassificada;
import com.banco.reclamacoes.domain.event.ReclamacaoCriada;
import com.banco.reclamacoes.domain.event.SlaCalculado;
import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import com.banco.reclamacoes.domain.port.GeradorProtocoloServico;
import com.banco.reclamacoes.domain.port.ProvedorIdentidadeReclamacao;
import com.banco.reclamacoes.domain.port.ServicoCalculoSla;
import com.banco.reclamacoes.domain.valueobject.Classificacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.Sla;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public class Reclamacao {

    private final ReclamacaoId id;
    private final SolicitacaoId solicitacaoId;
    private final Protocolo protocolo;
    private final ClienteId clienteId;
    private final CanalOrigem canalOrigem;
    private final DescricaoReclamacao descricao;
    private StatusReclamacao status;
    private Classificacao classificacao;
    private ResultadoClassificacao resultadoClassificacao;
    private final Instant dataRecebimento;
    private Sla sla;
    private boolean alertaSlaEmitido;
    private boolean integracaoRegistrada;
    private final CorrelationId correlationId;
    private final IdempotencyKey idempotencyKey;
    private final List<MetadadoAnexo> anexos;
    private final List<HistoricoInterno> historico;
    private final Deque<EventoDominio> eventosDominio;

    private Reclamacao(
        final ReclamacaoId id,
        final SolicitacaoId solicitacaoId,
        final Protocolo protocolo,
        final ClienteId clienteId,
        final CanalOrigem canalOrigem,
        final DescricaoReclamacao descricao,
        final Instant dataRecebimento,
        final CorrelationId correlationId,
        final IdempotencyKey idempotencyKey,
        final List<MetadadoAnexo> anexos
    ) {
        this.id = id;
        this.solicitacaoId = solicitacaoId;
        this.protocolo = protocolo;
        this.clienteId = clienteId;
        this.canalOrigem = canalOrigem;
        this.descricao = descricao;
        this.dataRecebimento = dataRecebimento;
        this.correlationId = correlationId;
        this.idempotencyKey = idempotencyKey;
        this.anexos = new ArrayList<>(anexos);
        this.status = StatusReclamacao.ABERTA;
        this.historico = new ArrayList<>();
        this.eventosDominio = new ArrayDeque<>();
    }

    /**
     * Reidratação usada apenas por adaptadores de persistência autorizados a reconstruir o agregado.
     */
    public static Reclamacao restaurarPersistida(final ReclamacaoPersistenciaSnapshot snapshot) {
        Objects.requireNonNull(snapshot);
        final var reclamacao =
            new Reclamacao( snapshot.id(), snapshot.solicitacaoId(), snapshot.protocolo(), snapshot.clienteId(), snapshot.canalOrigem(), snapshot.descricao(), snapshot.dataRecebimento(), snapshot.correlationId(), snapshot.idempotencyKey(), snapshot.anexos());
        reclamacao.status = snapshot.status();
        reclamacao.classificacao = snapshot.classificacao();
        reclamacao.resultadoClassificacao = snapshot.resultadoClassificacao();
        reclamacao.sla = snapshot.sla();
        reclamacao.alertaSlaEmitido = snapshot.alertaSlaEmitido();
        reclamacao.integracaoRegistrada = snapshot.integracaoRegistrada();
        reclamacao.historico.clear();
        reclamacao.historico.addAll(snapshot.historicosPersistidos());
        reclamacao.eventosDominio.clear();
        return reclamacao;
    }

    /**
     * Usar {@link FabricaReclamacao} na camada de aplicação; este método permanece visível ao pacote para a fábrica
     * e testes do mesmo módulo.
     */
    static Reclamacao criarComServicos(
            final DadosNovaReclamacao dados,
            final ResultadoClassificacao resultadoClassificacao,
            final ParametrosSla parametrosSla,
            final Clock relogioDomain,
            final ProvedorIdentidadeReclamacao provedorIdentidade,
            final GeradorProtocoloServico geradorProtocolo,
            final ServicoCalculoSla calculoSla) {
        Objects.requireNonNull(dados);
        Objects.requireNonNull(resultadoClassificacao);
        Objects.requireNonNull(parametrosSla);
        Objects.requireNonNull(relogioDomain);
        Objects.requireNonNull(provedorIdentidade);
        Objects.requireNonNull(geradorProtocolo);
        Objects.requireNonNull(calculoSla);

        final var agora = Instant.now(relogioDomain);
        final var id = provedorIdentidade.novaIdentidade();
        final var protocolo = geradorProtocolo.gerarParaDataRecebimento(dados.dataRecebimento());
        final var reclamacao =
            new Reclamacao( id, dados.solicitacaoId(), protocolo, dados.clienteId(), dados.canalOrigem(), dados.descricao(), dados.dataRecebimento(), dados.correlationId(), dados.idempotencyKey(), dados.anexos());
        reclamacao.registrarHistorico("CRIACAO", "Reclamação oficial instanciada", agora);
        reclamacao.eventosDominio.add(
            new ReclamacaoCriada( id, dados.solicitacaoId(), dados.clienteId(), protocolo, dados.correlationId(), agora));

        reclamacao.classificar(resultadoClassificacao, relogioDomain);
        reclamacao.definirSla(calculoSla.calcular(dados.dataRecebimento(), parametrosSla), relogioDomain);
        return reclamacao;
    }

    public void classificar(final ResultadoClassificacao resultado, final Clock relogioDomain) {
        Objects.requireNonNull(resultado);
        final var agora = Instant.now(relogioDomain);
        this.resultadoClassificacao = resultado;
        this.classificacao =
            Classificacao.de(resultado.categoriaPrincipal(), resultado.confianca());
        if (resultado.requerRevisaoManual()) {
            this.status = StatusReclamacao.EM_ANALISE;
        }
        registrarHistorico("CLASSIFICACAO", "Classificação automática aplicada", agora);
        this.eventosDominio.add(new ReclamacaoClassificada(
            id,
            resultado.categoriaPrincipal(),
            resultado.confianca(),
            agora));
    }

    public void definirSla(final Sla novoSla, final Clock relogioDomain) {
        Objects.requireNonNull(novoSla);
        final var agora = Instant.now(relogioDomain);
        this.sla = novoSla;
        registrarHistorico("SLA", "SLA calculado para a reclamação", agora);
        this.eventosDominio.add(new SlaCalculado(
            id,
            novoSla.deadline(),
            novoSla.alerta(),
            agora));
    }

    public void registrarHistorico(final String tipo, final String detalhe, final Instant ocorridoEm) {
        if (tipo == null || tipo.isBlank()) {
            throw new ViolacaoDominioException("tipo de histórico inválido");
        }
        historico.add(new HistoricoInterno(tipo, detalhe == null ? "" : detalhe, ocorridoEm));
    }

    public void marcarAlertaSlaEmitido(final Clock relogioDomain) {
        if (alertaSlaEmitido) {
            return;
        }
        final var agora = Instant.now(relogioDomain);
        this.alertaSlaEmitido = true;
        registrarHistorico("SLA_ALERTA", "Alerta de SLA emitido", agora);
        this.eventosDominio.add(new AlertaSlaEmitido(id, protocolo, agora));
    }

    public void registrarEnvioIntegracao(final Clock relogioDomain) {
        if (integracaoRegistrada) {
            return;
        }
        if (classificacao == null || sla == null) {
            throw new ViolacaoDominioException("fluxo incompleto para integração");
        }
        final var agora = Instant.now(relogioDomain);
        this.integracaoRegistrada = true;
        registrarHistorico("INTEGRACAO", "Publicação para integração externa registrada", agora);
        this.eventosDominio.add(new IntegracaoSolicitada(
            id,
            protocolo,
            clienteId,
            classificacao.categoriaPrincipal(),
            status,
            dataRecebimento,
            correlationId,
            agora));
    }

    public List<EventoDominio> drenarEventosDominio() {
        final var copia = new ArrayList<>(eventosDominio);
        eventosDominio.clear();
        return copia;
    }

    public ReclamacaoId id() {
        return id;
    }

    public SolicitacaoId solicitacaoId() {
        return solicitacaoId;
    }

    public Protocolo protocolo() {
        return protocolo;
    }

    public ClienteId clienteId() {
        return clienteId;
    }

    public CanalOrigem canalOrigem() {
        return canalOrigem;
    }

    public DescricaoReclamacao descricao() {
        return descricao;
    }

    public StatusReclamacao status() {
        return status;
    }

    public Classificacao classificacao() {
        return classificacao;
    }

    public ResultadoClassificacao resultadoClassificacao() {
        return resultadoClassificacao;
    }

    public Instant dataRecebimento() {
        return dataRecebimento;
    }

    public Sla sla() {
        return sla;
    }

    public boolean alertaSlaEmitido() {
        return alertaSlaEmitido;
    }

    public boolean integracaoRegistrada() {
        return integracaoRegistrada;
    }

    public CorrelationId correlationId() {
        return correlationId;
    }

    public IdempotencyKey idempotencyKey() {
        return idempotencyKey;
    }

    public List<MetadadoAnexo> anexos() {
        return Collections.unmodifiableList(anexos);
    }

    public List<HistoricoInterno> historicoInterno() {
        return Collections.unmodifiableList(historico);
    }
}
