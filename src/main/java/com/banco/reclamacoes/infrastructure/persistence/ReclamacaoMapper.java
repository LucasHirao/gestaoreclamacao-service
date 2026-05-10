package com.banco.reclamacoes.infrastructure.persistence;

import com.banco.reclamacoes.domain.model.CanalOrigem;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.model.HistoricoInterno;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.model.ReclamacaoPersistenciaSnapshot;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.Classificacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.CorrelationId;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.CategoriaPontuacao;
import com.banco.reclamacoes.domain.valueobject.MontagemResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import com.banco.reclamacoes.domain.valueobject.ScoreClassificacao;
import com.banco.reclamacoes.domain.valueobject.Sla;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import com.banco.reclamacoes.domain.valueobject.PrazoSla;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReclamacaoMapper {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public final ReclamacaoJpaEntity paraEntidade(final Reclamacao reclamacao) throws JsonProcessingException {
        final var entity = new ReclamacaoJpaEntity();
        entity.id = reclamacao.id().valor();
        entity.solicitacaoId = reclamacao.solicitacaoId().valor();
        entity.protocolo = reclamacao.protocolo().valor();
        entity.clienteId = reclamacao.clienteId().valor();
        entity.canalOrigem = reclamacao.canalOrigem().name();
        entity.descricao = reclamacao.descricao().texto();
        entity.status = reclamacao.status().name();
        if (reclamacao.classificacao() != null) {
            entity.categoriaPrincipal = reclamacao.classificacao().categoriaPrincipal().name();
            entity.confiancaClassificacao = reclamacao.classificacao().confianca().name();
        }
        entity.dataRecebimento = reclamacao.dataRecebimento();
        if (reclamacao.sla() != null) {
            entity.deadlineAt = reclamacao.sla().deadline().instant();
            entity.alertAt = reclamacao.sla().alerta().instant();
        }
        entity.alertaEmitido = reclamacao.alertaSlaEmitido();
        entity.integracaoPublicada = reclamacao.integracaoRegistrada();
        entity.correlationId = reclamacao.correlationId().valor();
        entity.idempotencyKey = reclamacao.idempotencyKey().valor();
        final var agora = Instant.now(clock);
        if (entity.getCreatedAt() == null) {
            entity.createdAt = agora;
        }
        entity.updatedAt = agora;

        if (reclamacao.resultadoClassificacao() != null) {
            final var snapshot = ResultadoClassificacaoPersistivel.of(reclamacao.resultadoClassificacao());
            entity.resultadoClassificacaoJson = objectMapper.writeValueAsString(snapshot);
            final var detalhe = new ClassificacaoResultadoJpaEntity();
            detalhe.reclamacao = entity;
            detalhe.payload = objectMapper.writeValueAsString(snapshot);
            entity.classificacaoResultado = detalhe;
        }

        entity.getHistoricos().clear();
        for (final var historico : reclamacao.historicoInterno()) {
            final var linha = new ReclamacaoHistoricoJpaEntity();
            linha.reclamacao = entity;
            linha.tipo = historico.tipo();
            linha.detalhe = historico.detalhe();
            linha.ocorridoEm = historico.ocorridoEm();
            entity.getHistoricos().add(linha);
        }

        entity.getAnexos().clear();
        for (final var anexo : reclamacao.anexos()) {
            final var linha = new ReclamacaoAnexoJpaEntity();
            linha.reclamacao = entity;
            linha.referencia = anexo.referencia();
            linha.tipoMime = anexo.tipoMime();
            try {
                linha.atributosJson = objectMapper.writeValueAsString(anexo.atributos());
            } catch (final JsonProcessingException ignored) {
                linha.atributosJson = "{}";
            }
            entity.getAnexos().add(linha);
        }

        entity.getAgendaSla().clear();
        if (reclamacao.sla() != null) {
            final var agenda = new AgendaSlaJpaEntity();
            agenda.reclamacao = entity;
            agenda.alertAt = reclamacao.sla().alerta().instant();
            agenda.deadlineAt = reclamacao.sla().deadline().instant();
            agenda.processado = reclamacao.alertaSlaEmitido();
            entity.getAgendaSla().add(agenda);
        }

        return entity;
    }

    public final void mesclarNaEntidadeExistente(final Reclamacao dominio, final ReclamacaoJpaEntity existente)
        throws JsonProcessingException {
        existente.status = dominio.status().name();
        if (dominio.classificacao() != null) {
            existente.categoriaPrincipal = dominio.classificacao().categoriaPrincipal().name();
            existente.confiancaClassificacao = dominio.classificacao().confianca().name();
        }
        if (dominio.sla() != null) {
            existente.deadlineAt = dominio.sla().deadline().instant();
            existente.alertAt = dominio.sla().alerta().instant();
        }
        existente.alertaEmitido = dominio.alertaSlaEmitido();
        existente.integracaoPublicada = dominio.integracaoRegistrada();
        existente.updatedAt = Instant.now(clock);
        if (dominio.resultadoClassificacao() != null) {
            final var snapshot = ResultadoClassificacaoPersistivel.of(dominio.resultadoClassificacao());
            existente.resultadoClassificacaoJson = objectMapper.writeValueAsString(snapshot);
            var detalhe = existente.getClassificacaoResultado();
            if (detalhe == null) {
                detalhe = new ClassificacaoResultadoJpaEntity();
                detalhe.reclamacao = existente;
                existente.classificacaoResultado = detalhe;
            }
            detalhe.payload = objectMapper.writeValueAsString(snapshot);
        }

        existente.getHistoricos().clear();
        for (final var historico : dominio.historicoInterno()) {
            final var linha = new ReclamacaoHistoricoJpaEntity();
            linha.reclamacao = existente;
            linha.tipo = historico.tipo();
            linha.detalhe = historico.detalhe();
            linha.ocorridoEm = historico.ocorridoEm();
            existente.getHistoricos().add(linha);
        }

        existente.getAnexos().clear();
        for (final var anexo : dominio.anexos()) {
            final var linha = new ReclamacaoAnexoJpaEntity();
            linha.reclamacao = existente;
            linha.referencia = anexo.referencia();
            linha.tipoMime = anexo.tipoMime();
            try {
                linha.atributosJson = objectMapper.writeValueAsString(anexo.atributos());
            } catch (final JsonProcessingException ignored) {
                linha.atributosJson = "{}";
            }
            existente.getAnexos().add(linha);
        }

        existente.getAgendaSla().clear();
        if (dominio.sla() != null) {
            final var agenda = new AgendaSlaJpaEntity();
            agenda.reclamacao = existente;
            agenda.alertAt = dominio.sla().alerta().instant();
            agenda.deadlineAt = dominio.sla().deadline().instant();
            agenda.processado = dominio.alertaSlaEmitido();
            existente.getAgendaSla().add(agenda);
        }
    }

    public final Reclamacao paraDominio(final ReclamacaoJpaEntity entity) throws IOException {
        final var historicos = new ArrayList<HistoricoInterno>();
        entity.getHistoricos().stream()
            .sorted((a, b) -> a.getOcorridoEm().compareTo(b.getOcorridoEm()))
            .forEach(h -> historicos.add(new HistoricoInterno(h.getTipo(), h.getDetalhe(), h.getOcorridoEm())));

        final var anexos = new ArrayList<MetadadoAnexo>();
        for (final var anexo : entity.getAnexos()) {
            final Map<String, String> atributos;
            final String metadados = anexo.getAtributosJson();
            if (metadados == null || metadados.isBlank()) {
                atributos = Map.of();
            } else {
                atributos = objectMapper.readValue(metadados, new TypeReference<>() {});
            }
            anexos.add(new MetadadoAnexo(anexo.getReferencia(), anexo.getTipoMime(), atributos));
        }

        Classificacao classificacao = null;
        ResultadoClassificacao resultado = null;
        if (entity.getCategoriaPrincipal() != null && entity.getConfiancaClassificacao() != null) {
            classificacao =
                Classificacao.de(
                    CategoriaReclamacao.valueOf(entity.getCategoriaPrincipal()),
                    ConfiancaClassificacao.valueOf(entity.getConfiancaClassificacao()));
        }
        if (entity.getResultadoClassificacaoJson() != null) {
            final var snapshot =
                objectMapper.readValue(entity.getResultadoClassificacaoJson(), ResultadoClassificacaoPersistivel.class);
            resultado = paraResultadoDominio(snapshot);
        }

        Sla sla = null;
        if (entity.getDeadlineAt() != null && entity.getAlertAt() != null) {
            sla = Sla.of(PrazoSla.de(entity.getDeadlineAt()), PrazoSla.de(entity.getAlertAt()));
        }

        final var snapshot =
            ReclamacaoPersistenciaSnapshot.builder()
                .id(ReclamacaoId.de(entity.getId()))
                .solicitacaoId(SolicitacaoId.de(entity.getSolicitacaoId()))
                .protocolo(Protocolo.de(entity.getProtocolo()))
                .clienteId(ClienteId.de(entity.getClienteId()))
                .canalOrigem(CanalOrigem.valueOf(entity.getCanalOrigem()))
                .descricao(DescricaoReclamacao.de(entity.getDescricao()))
                .status(StatusReclamacao.valueOf(entity.getStatus()))
                .classificacao(classificacao)
                .resultadoClassificacao(resultado)
                .dataRecebimento(entity.getDataRecebimento())
                .sla(sla)
                .alertaSlaEmitido(entity.isAlertaEmitido())
                .integracaoRegistrada(entity.isIntegracaoPublicada())
                .correlationId(CorrelationId.de(entity.getCorrelationId()))
                .idempotencyKey(IdempotencyKey.de(entity.getIdempotencyKey()))
                .anexos(anexos)
                .historicosPersistidos(historicos)
                .build();
        return Reclamacao.restaurarPersistida(snapshot);
    }

    private ResultadoClassificacao paraResultadoDominio(final ResultadoClassificacaoPersistivel snapshot) {
        final var candidatas = new ArrayList<CategoriaPontuacao>();
        for (final Map<String, Object> entrada : snapshot.categoriasCandidatas) {
            final var categoria = (String) entrada.get("categoria");
            final var score = ((Number) entrada.get("score")).intValue();
            candidatas.add(
                CategoriaPontuacao.de(CategoriaReclamacao.valueOf(categoria), ScoreClassificacao.de(score)));
        }
        final var montagem =
            MontagemResultadoClassificacao.builder()
                .categoriaPrincipal(CategoriaReclamacao.valueOf(snapshot.categoriaPrincipal))
                .categoriasCandidatas(candidatas)
                .scorePrincipal(ScoreClassificacao.de(snapshot.scorePrincipal))
                .confianca(snapshot.confianca)
                .justificativas(snapshot.justificativas)
                .requerRevisaoManual(snapshot.requerRevisaoManual)
                .build();
        return ResultadoClassificacao.montar(montagem);
    }
}
