package com.banco.reclamacoes.application.usecase;

import com.banco.reclamacoes.application.command.AnexoSolicitacaoCommand;
import com.banco.reclamacoes.application.command.ProcessarSolicitacaoPadronizadaCommand;
import com.banco.reclamacoes.application.port.MapeadorAnexosSolicitacao;
import com.banco.reclamacoes.application.port.input.RegistrarSolicitacaoPadronizadaPort;
import com.banco.reclamacoes.application.port.output.HistoricoClientePort;
import com.banco.reclamacoes.domain.port.MotorClassificacaoPort;
import com.banco.reclamacoes.application.port.output.ParametrosSlaPort;
import com.banco.reclamacoes.application.port.output.IntegracaoPublicacaoComando;
import com.banco.reclamacoes.application.port.output.ProcessamentoSolicitacaoMarcadoresPort;
import com.banco.reclamacoes.application.port.output.PublicadorIntegracaoPort;
import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import com.banco.reclamacoes.domain.policy.PoliticaConteudoSolicitacaoPadronizada;
import com.banco.reclamacoes.domain.model.CanalOrigem;
import com.banco.reclamacoes.domain.model.DadosNovaReclamacao;
import com.banco.reclamacoes.domain.model.FabricaReclamacao;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessarSolicitacaoPadronizadaUseCase implements RegistrarSolicitacaoPadronizadaPort {

    private static final Logger log = LoggerFactory.getLogger(ProcessarSolicitacaoPadronizadaUseCase.class);

    private final ReclamacaoRepositoryPort repositorio;
    private final MotorClassificacaoPort motorClassificacao;
    private final ParametrosSlaPort parametrosSlaPort;
    private final PublicadorIntegracaoPort publicadorIntegracao;
    private final HistoricoClientePort historicoClientePort;
    private final ProcessamentoSolicitacaoMarcadoresPort marcadores;
    private final Clock clock;
    private final FabricaReclamacao fabricaReclamacao;
    private final MapeadorAnexosSolicitacao mapeadorAnexos;

    @Transactional
    @Override
    public Optional<UUID> processar(final ProcessarSolicitacaoPadronizadaCommand command) {
        try {
            validarConteudoNegocial(command);

            final var anexosDominio = mapeadorAnexos.paraDominio(command.anexos());
            final var parametrosSla = parametrosSlaPort.carregar();
            final var entrada = EntradaPadronizada.aPartirDe(command);

            log.info("Solicitação padronizada recebida canal={} idempotenciaPresente=true", entrada.canal());

            final var atalhoIdempotente = buscarIdSeSolicitacaoJaProcessada(entrada);
            if (atalhoIdempotente.isPresent()) {
                return atalhoIdempotente;
            }

            return Optional.of(novaReclamacaoCompleta(command, anexosDominio, parametrosSla, entrada));
        } catch (final ViolacaoDominioException erroDominio) {
            log.error("Erro ao processar solicitação padronizada: {}", erroDominio.getMessage());
            throw erroDominio;
        } catch (final RuntimeException erro) {
            log.error("Erro inesperado ao processar solicitação padronizada", erro);
            throw erro;
        }
    }

    private static void validarConteudoNegocial(final ProcessarSolicitacaoPadronizadaCommand command) {
        PoliticaConteudoSolicitacaoPadronizada.validar(
                command.descricao(),
                command.metadados(),
                atributosPorAnexoParaValidacao(command.anexos()));
    }

    private static List<Map<String, String>> atributosPorAnexoParaValidacao(
            final List<AnexoSolicitacaoCommand> anexos) {
        if (anexos == null) {
            return List.of();
        }
        return anexos.stream()
                .map(a -> a.atributos() == null ? Map.<String, String>of() : a.atributos())
                .toList();
    }

    private Optional<UUID> buscarIdSeSolicitacaoJaProcessada(final EntradaPadronizada entrada) {
        final var porSolicitacao = repositorio.buscarPorSolicitacaoId(entrada.solicitacaoId());
        if (porSolicitacao.isPresent()) {
            log.info("Fluxo idempotente: solicitacao já processada");
            return Optional.of(porSolicitacao.get().id().valor());
        }
        final var porChave = repositorio.buscarPorIdempotencyKey(entrada.idempotencyKey());
        if (porChave.isPresent()) {
            log.info("Fluxo idempotente: mesma idempotencyKey já registrada");
            return Optional.of(porChave.get().id().valor());
        }
        return Optional.empty();
    }

    private UUID novaReclamacaoCompleta(
            final ProcessarSolicitacaoPadronizadaCommand command,
            final List<MetadadoAnexo> anexosDominio,
            final ParametrosSla parametrosSla,
            final EntradaPadronizada entrada) {

        final var dados =
                DadosNovaReclamacao.builder()
                        .solicitacaoId(entrada.solicitacaoId())
                        .clienteId(entrada.clienteId())
                        .canalOrigem(entrada.canal())
                        .descricao(entrada.descricao())
                        .dataRecebimento(command.dataRecebimento())
                        .correlationId(entrada.correlationId())
                        .idempotencyKey(entrada.idempotencyKey())
                        .anexos(anexosDominio)
                        .build();

        final var resultadoClassificacao = motorClassificacao.classificar(entrada.descricao());
        var reclamacao = fabricaReclamacao.nova(dados, resultadoClassificacao, parametrosSla, clock);

        var persistida = repositorio.salvar(reclamacao);
        registrarHistoricoCliente(persistida);
        marcadores.aposPrimeiraPersistencia(persistida, resultadoClassificacao);

        publicadorIntegracao.publicar(comandoIntegracaoDe(persistida));
        persistida.registrarEnvioIntegracao(clock);
        persistida.drenarEventosDominio();
        persistida = repositorio.salvar(persistida);

        log.info(
                "Reclamação persistida protocolo={} categoriaPrincipal={}",
                persistida.protocolo().valor(),
                persistida.classificacao().categoriaPrincipal());

        return persistida.id().valor();
    }

    private static IntegracaoPublicacaoComando comandoIntegracaoDe(final Reclamacao salva) {
        return IntegracaoPublicacaoComando.builder()
                .reclamacaoId(salva.id())
                .protocolo(salva.protocolo())
                .clienteId(salva.clienteId())
                .categoria(salva.classificacao().categoriaPrincipal())
                .status(salva.status())
                .dataRecebimento(salva.dataRecebimento())
                .correlationId(salva.correlationId())
                .build();
    }

    private void registrarHistoricoCliente(final Reclamacao salva) {
        historicoClientePort.registrarMovimentoCliente(
                salva.clienteId(), "ABERTURA_RECLAMACAO", salva.protocolo().valor());
    }

    /**
     * Valores de domínio já resolvidos a partir do comando (evita repetir {@code SolicitacaoId.de(...)} no fluxo).
     */
    private record EntradaPadronizada(
            SolicitacaoId solicitacaoId,
            IdempotencyKey idempotencyKey,
            ClienteId clienteId,
            CorrelationId correlationId,
            DescricaoReclamacao descricao,
            CanalOrigem canal) {

        static EntradaPadronizada aPartirDe(final ProcessarSolicitacaoPadronizadaCommand c) {
            return new EntradaPadronizada(
                    SolicitacaoId.de(c.solicitacaoId()),
                    IdempotencyKey.de(c.idempotencyKey()),
                    ClienteId.de(c.clienteId()),
                    CorrelationId.de(c.correlationId()),
                    DescricaoReclamacao.de(c.descricao()),
                    Optional.ofNullable(c.canalOrigem()).orElse(CanalOrigem.DIGITAL));
        }
    }
}
