package com.banco.reclamacoes.infrastructure.config;

import com.banco.reclamacoes.domain.model.FabricaReclamacao;
import com.banco.reclamacoes.domain.port.GeradorProtocoloServico;
import com.banco.reclamacoes.domain.port.ProvedorIdentidadeReclamacao;
import com.banco.reclamacoes.domain.port.ServicoCalculoSla;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FabricaReclamacaoConfig {

    @Bean
    FabricaReclamacao fabricaReclamacao(
            final ProvedorIdentidadeReclamacao provedorIdentidade,
            final GeradorProtocoloServico geradorProtocolo,
            final ServicoCalculoSla calculoSla) {
        return new FabricaReclamacao(provedorIdentidade, geradorProtocolo, calculoSla);
    }
}
