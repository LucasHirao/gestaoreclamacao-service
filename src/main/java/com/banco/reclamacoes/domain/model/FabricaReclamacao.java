package com.banco.reclamacoes.domain.model;

import com.banco.reclamacoes.domain.port.GeradorProtocoloServico;
import com.banco.reclamacoes.domain.port.ProvedorIdentidadeReclamacao;
import com.banco.reclamacoes.domain.port.ServicoCalculoSla;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import java.time.Clock;
import java.util.Objects;

/**
 * Monta {@link Reclamacao} nova apenas com dependências abstratas (inversão de dependência).
 * Instanciada e injetada a partir da composição (Spring / testes).
 */
public final class FabricaReclamacao {

    private final ProvedorIdentidadeReclamacao provedorIdentidade;
    private final GeradorProtocoloServico geradorProtocolo;
    private final ServicoCalculoSla calculoSla;

    public FabricaReclamacao(
            final ProvedorIdentidadeReclamacao provedorIdentidade,
            final GeradorProtocoloServico geradorProtocolo,
            final ServicoCalculoSla calculoSla) {
        this.provedorIdentidade = Objects.requireNonNull(provedorIdentidade);
        this.geradorProtocolo = Objects.requireNonNull(geradorProtocolo);
        this.calculoSla = Objects.requireNonNull(calculoSla);
    }

    public Reclamacao nova(
            final DadosNovaReclamacao dados,
            final ResultadoClassificacao resultadoClassificacao,
            final ParametrosSla parametrosSla,
            final Clock relogioDomain) {
        return Reclamacao.criarComServicos(
                dados, resultadoClassificacao, parametrosSla, relogioDomain, provedorIdentidade, geradorProtocolo, calculoSla);
    }
}
