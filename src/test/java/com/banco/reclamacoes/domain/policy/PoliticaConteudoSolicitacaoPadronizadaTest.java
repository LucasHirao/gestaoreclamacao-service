package com.banco.reclamacoes.domain.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PoliticaConteudoSolicitacaoPadronizadaTest {

    @Test
    void aceitaDescricaoLongaESnakeCaseValido() {
        assertThatCode(
                () ->
                    PoliticaConteudoSolicitacaoPadronizada.validar(
                        "Relato longo o suficiente para passar no tamanho mínimo da fila POC de triagem",
                        Map.of("canal_origem", "app")))
            .doesNotThrowAnyException();
    }

    @Test
    void rejeitaDescricaoCurta() {
        assertThatThrownBy(() -> PoliticaConteudoSolicitacaoPadronizada.validar("curta", Map.of()))
            .isInstanceOf(ViolacaoDominioException.class)
            .hasMessageContaining("fila POC");
    }

    @Test
    void rejeitaMarcadorReservadoPocNaDescricao() {
        assertThatThrownBy(
                () ->
                    PoliticaConteudoSolicitacaoPadronizada.validar(
                        "Texto inicial longo o bastante [[PREENCHA_AQUI]] continuação do relato do cliente.",
                        Map.of()))
            .isInstanceOf(ViolacaoDominioException.class)
            .hasMessageContaining("marcador reservado");
    }

    @Test
    void rejeitaChaveMetadadoForaDoSnakeCase() {
        assertThatThrownBy(
                () ->
                    PoliticaConteudoSolicitacaoPadronizada.validar(
                        "Descricao suficientemente longa para a politica POC atual.",
                        Map.of("CanalOrigem", "x")))
            .isInstanceOf(ViolacaoDominioException.class)
            .hasMessageContaining("snake_case");
    }

    @Test
    void rejeitaValorMetadadoAcimaDoLimitePoc() {
        final var valorLongo = "x".repeat(281);
        assertThatThrownBy(
                () ->
                    PoliticaConteudoSolicitacaoPadronizada.validar(
                        "Descricao suficientemente longa para a politica POC atual.", Map.of("x", valorLongo)))
            .isInstanceOf(ViolacaoDominioException.class)
            .hasMessageContaining("280");
    }

    @Test
    void ignoraListaDeAnexosNulaOuElementoMapNuloNaIteracao() {
        assertThatCode(() -> PoliticaConteudoSolicitacaoPadronizada.validar("texto suficientemente longo", Map.of(), null))
            .doesNotThrowAnyException();
        assertThatCode(
                () ->
                    PoliticaConteudoSolicitacaoPadronizada.validar(
                        "texto suficientemente longo", Map.of(), Collections.singletonList(null)))
            .doesNotThrowAnyException();
    }

    @Test
    void validaValorNuloOuChaveNulaSemLancarQuandoDentroDosLimitesPoc() {
        Map<String, String> comValorNulo = new HashMap<>();
        comValorNulo.put("ref_protocolo", null);
        assertThatCode(() -> PoliticaConteudoSolicitacaoPadronizada.validar("texto suficientemente longo", comValorNulo))
            .doesNotThrowAnyException();

        Map<String, String> comChaveNula = new HashMap<>();
        comChaveNula.put(null, "valor ok longo suficiente");
        assertThatCode(() -> PoliticaConteudoSolicitacaoPadronizada.validar("texto suficientemente longo", comChaveNula))
            .doesNotThrowAnyException();
    }

    @Test
    void rejeitaValorLongoEmAtributoDeAnexo() {
        final var grande = "z".repeat(300);
        assertThatThrownBy(
                () ->
                    PoliticaConteudoSolicitacaoPadronizada.validar(
                        "Descricao suficientemente longa para a politica POC atual.",
                        Map.of(),
                        List.of(Map.of("observacao", grande))))
            .isInstanceOf(ViolacaoDominioException.class);
    }
}
