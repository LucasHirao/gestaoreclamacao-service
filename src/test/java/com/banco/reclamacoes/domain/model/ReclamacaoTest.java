package com.banco.reclamacoes.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.valueobject.CategoriaPontuacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
import com.banco.reclamacoes.domain.valueobject.MontagemResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ScoreClassificacao;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import com.banco.reclamacoes.infrastructure.domain.GeradorProtocoloAleatorio;
import com.banco.reclamacoes.infrastructure.domain.ProvedorIdentidadeReclamacaoUuid;
import com.banco.reclamacoes.infrastructure.domain.ServicoCalculoSlaCorridos;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReclamacaoTest {

    private static final ParametrosSla PARAMETROS_SLA_TESTE = ParametrosSla.of(10, 2, 100);

    private final FabricaReclamacao fabrica =
        new FabricaReclamacao(
            new ProvedorIdentidadeReclamacaoUuid(),
            new GeradorProtocoloAleatorio(),
            new ServicoCalculoSlaCorridos());

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-01T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void deveCriarReclamacaoCompletaFluxoFeliz() {
        final var dados =
            DadosNovaReclamacao.builder()
                .solicitacaoId(SolicitacaoId.de("SOL-001"))
                .clienteId(ClienteId.de("CLI-8842"))
                .canalOrigem(CanalOrigem.DIGITAL)
                .descricao(DescricaoReclamacao.de("Estou enfrentando um problema bem grave com o aplicativo."))
                .dataRecebimento(Instant.parse("2026-05-01T09:15:00Z"))
                .correlationId(CorrelationId.de("COR-ABC"))
                .idempotencyKey(IdempotencyKey.de("idem-xyz"))
                .anexos(List.of(new MetadadoAnexo("s3://bucket/x", "image/png", Map.of())))
                .build();

        final var montagem =
            MontagemResultadoClassificacao.builder()
                .categoriaPrincipal(CategoriaReclamacao.FRAUDE)
                .categoriasCandidatas(
                    List.of(
                        CategoriaPontuacao.de(CategoriaReclamacao.FRAUDE, ScoreClassificacao.de(30)),
                        CategoriaPontuacao.de(CategoriaReclamacao.CARTAO, ScoreClassificacao.de(10))))
                .scorePrincipal(ScoreClassificacao.de(30))
                .confianca(ConfiancaClassificacao.ALTA)
                .justificativas(List.of("fraude"))
                .requerRevisaoManual(false)
                .build();
        final var resultado = ResultadoClassificacao.montar(montagem);

        final var reclamacao = fabrica.nova(dados, resultado, PARAMETROS_SLA_TESTE, clock);

        final var slaEsperado = new ServicoCalculoSlaCorridos().calcular(dados.dataRecebimento(), PARAMETROS_SLA_TESTE);

        assertThat(reclamacao.protocolo()).isNotNull();
        assertThat(reclamacao.resultadoClassificacao().categoriaPrincipal()).isEqualTo(CategoriaReclamacao.FRAUDE);
        assertThat(reclamacao.sla()).isEqualTo(slaEsperado);
        assertThat(reclamacao.integracaoRegistrada()).isFalse();
        reclamacao.registrarEnvioIntegracao(clock);
        assertThat(reclamacao.integracaoRegistrada()).isTrue();
    }

    @Test
    void naoPermiteClienteVazioAoCriar() {
        assertThatThrownBy(
                () -> {
                    final var dados =
                        DadosNovaReclamacao.builder()
                            .solicitacaoId(SolicitacaoId.de("S"))
                            .clienteId(ClienteId.de("  "))
                            .canalOrigem(CanalOrigem.DIGITAL)
                            .descricao(
                                DescricaoReclamacao.de(
                                    "Texto suficientemente longo para passar pela validação mínima de dez caracteres."))
                            .dataRecebimento(Instant.now())
                            .correlationId(CorrelationId.de("COR"))
                            .idempotencyKey(IdempotencyKey.de("idem"))
                            .anexos(List.of())
                            .build();
                    fabrica.nova(dados, resultadoMinimo(), PARAMETROS_SLA_TESTE, clock);
                })
            .isInstanceOf(ViolacaoDominioException.class);
    }

    @Test
    void naoPermiteDescricaoCurtaAoCriar() {
        assertThatThrownBy(
                () -> {
                    final var dados =
                        DadosNovaReclamacao.builder()
                            .solicitacaoId(SolicitacaoId.de("S"))
                            .clienteId(ClienteId.de("CLI"))
                            .canalOrigem(CanalOrigem.DIGITAL)
                            .descricao(DescricaoReclamacao.de("curta"))
                            .dataRecebimento(Instant.now())
                            .correlationId(CorrelationId.de("COR"))
                            .idempotencyKey(IdempotencyKey.de("idem"))
                            .anexos(List.of())
                            .build();
                    fabrica.nova(dados, resultadoMinimo(), PARAMETROS_SLA_TESTE, clock);
                })
            .isInstanceOf(ViolacaoDominioException.class);
    }

    @Test
    void alertaSlaEhIdempotente() {
        final var montagem =
            MontagemResultadoClassificacao.builder()
                .categoriaPrincipal(CategoriaReclamacao.CONTA_CORRENTE)
                .categoriasCandidatas(
                    List.of(
                        CategoriaPontuacao.de(
                            CategoriaReclamacao.CONTA_CORRENTE, ScoreClassificacao.de(14))))
                .scorePrincipal(ScoreClassificacao.de(14))
                .confianca(ConfiancaClassificacao.MEDIA)
                .justificativas(List.of())
                .requerRevisaoManual(false)
                .build();
        final var reclamacao = criarMinimaParaAlerta(ResultadoClassificacao.montar(montagem));
        reclamacao.marcarAlertaSlaEmitido(clock);
        reclamacao.marcarAlertaSlaEmitido(clock);
        assertThat(reclamacao.alertaSlaEmitido()).isTrue();
    }

    private static ResultadoClassificacao resultadoMinimo() {
        final var montagem =
            MontagemResultadoClassificacao.builder()
                .categoriaPrincipal(CategoriaReclamacao.OUTROS)
                .categoriasCandidatas(
                    List.of(CategoriaPontuacao.de(CategoriaReclamacao.OUTROS, ScoreClassificacao.de(1))))
                .scorePrincipal(ScoreClassificacao.de(1))
                .confianca(ConfiancaClassificacao.BAIXA)
                .justificativas(List.of())
                .requerRevisaoManual(false)
                .build();
        return ResultadoClassificacao.montar(montagem);
    }

    private Reclamacao criarMinimaParaAlerta(final ResultadoClassificacao resultado) {
        final var dados =
            DadosNovaReclamacao.builder()
                .solicitacaoId(SolicitacaoId.de("SOL-ALERTA"))
                .clienteId(ClienteId.de("CLI-1"))
                .canalOrigem(CanalOrigem.DIGITAL)
                .descricao(
                    DescricaoReclamacao.de(
                        "Relato detalhado sobre saldo e extrato da conta corrente com mais de dez caracteres."))
                .dataRecebimento(Instant.parse("2026-05-01T09:00:00Z"))
                .correlationId(CorrelationId.de("COR"))
                .idempotencyKey(IdempotencyKey.de("idem-alert"))
                .anexos(List.of())
                .build();
        return fabrica.nova(dados, resultado, PARAMETROS_SLA_TESTE, clock);
    }
}
