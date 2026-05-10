package com.banco.reclamacoes.application.classification;

import static org.assertj.core.api.Assertions.assertThat;

import com.banco.reclamacoes.domain.port.MotorClassificacaoPort;
import com.banco.reclamacoes.bootstrap.GestaoReclamacoesApplication;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = GestaoReclamacoesApplication.class,
    properties = {
        "spring.cloud.aws.sqs.enabled=false",
        "spring.cloud.aws.sns.enabled=false",
        "spring.cloud.aws.dynamodb.enabled=false"
    })
@ActiveProfiles("test")
@Tag("integration")
class ClassificadorPorRegrasPonderadasIntegrationTest {

    @Autowired
    private MotorClassificacaoPort motorClassificacaoPort;

    @Test
    @DisplayName("Classifica indício de fraude com cartão usando sinônimos")
    void classificaFraudeCartao() {
        var texto =
            DescricaoReclamacao.de(
                "não reconheço uma compra no meu cartão e quero contestar de imediato.");

        var resultado = motorClassificacaoPort.classificar(texto);

        assertThat(resultado.categoriaPrincipal()).isEqualTo(CategoriaReclamacao.FRAUDE);
        assertThat(resultado.justificativas())
            .anyMatch(
                j -> {
                    String l = j.toLowerCase();
                    return l.contains("compra")
                        || l.contains("cartao")
                        || l.contains("cartão")
                        || l.contains("reconhec")
                        || l.contains("fraude");
                });
    }

    @Test
    @DisplayName("Consulta ambígua entre cobrança e cartão retorna confiança não alta")
    void ambiguidadeCobrancaCartao() {
        var texto =
            DescricaoReclamacao.de(
                "Tive cobrança estranha na minha fatura mas também uso muito o cartão nos últimos dias.");

        var resultado = motorClassificacaoPort.classificar(texto);

        assertThat(resultado.categoriasCandidatas().size()).isGreaterThan(1);
        assertThat(resultado.confianca()).isNotEqualTo(ConfiancaClassificacao.ALTA);
    }
}
