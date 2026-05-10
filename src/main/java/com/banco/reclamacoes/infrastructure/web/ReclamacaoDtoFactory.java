package com.banco.reclamacoes.infrastructure.web;

import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.infrastructure.web.dto.CategoriaPontuacaoDto;
import com.banco.reclamacoes.infrastructure.web.dto.HistoricoLinhaDto;
import com.banco.reclamacoes.infrastructure.web.dto.ReclamacaoRespostaDto;
import com.banco.reclamacoes.infrastructure.web.dto.ReclamacaoResumoDto;
import com.banco.reclamacoes.infrastructure.web.dto.ResultadoClassificacaoRespostaDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReclamacaoDtoFactory {

    public ReclamacaoResumoDto resumo(final Reclamacao reclamacao) {
        return new ReclamacaoResumoDto( reclamacao.protocolo().valor(), reclamacao.clienteId().valor(), reclamacao.status().name(), reclamacao.classificacao() != null
                ? reclamacao.classificacao().categoriaPrincipal().name()
                : null, reclamacao.classificacao() != null
                ? reclamacao.classificacao().confianca().name()
                : null, reclamacao.sla() != null ? reclamacao.sla().deadline().instant() : null, reclamacao.sla() != null ? reclamacao.sla().alerta().instant() : null, reclamacao.alertaSlaEmitido(), reclamacao.dataRecebimento());
    }

    public ReclamacaoRespostaDto completo(final Reclamacao reclamacao) {
        ResultadoClassificacaoRespostaDto classificacaoDto = null;
        if (reclamacao.resultadoClassificacao() != null) {
            var rc = reclamacao.resultadoClassificacao();
            classificacaoDto =
                new ResultadoClassificacaoRespostaDto( rc.categoriaPrincipal().name(), rc.categoriasCandidatas().stream()
                        .map(
                            c ->
                                new CategoriaPontuacaoDto(
                                    c.categoria().name(), c.score().pontos()))
                        .toList(), rc.scorePrincipal().pontos(), rc.confianca().name(),
                    List.copyOf(rc.justificativas()), rc.requerRevisaoManual());
        }

        List<HistoricoLinhaDto> historico =
            reclamacao.historicoInterno().stream()
                .map(h -> new HistoricoLinhaDto(h.tipo(), h.detalhe(), h.ocorridoEm()))
                .toList();

        return new ReclamacaoRespostaDto( reclamacao.id().valor(), reclamacao.solicitacaoId().valor(), reclamacao.protocolo().valor(), reclamacao.clienteId().valor(), reclamacao.canalOrigem().name(), reclamacao.descricao().texto(), reclamacao.status().name(), classificacaoDto, reclamacao.sla() != null ? reclamacao.sla().deadline().instant() : null, reclamacao.sla() != null ? reclamacao.sla().alerta().instant() : null, reclamacao.alertaSlaEmitido(), reclamacao.integracaoRegistrada(), reclamacao.correlationId().valor(), reclamacao.idempotencyKey().valor(), reclamacao.dataRecebimento(), historico);
    }
}
