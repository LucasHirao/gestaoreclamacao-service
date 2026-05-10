package com.banco.reclamacoes.domain.policy;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Regras de conteúdo da solicitação padronizada (POC): limites artificiais para triagem e contrato de
 * metadados — sem validação de dados sensíveis.
 */
public final class PoliticaConteudoSolicitacaoPadronizada {

    /** Tamanho mínimo da descrição além do valor objeto {@code DescricaoReclamacao}. */
    private static final int DESCRICAO_MINIMA_EXTRA_POC = 20;

    /** Tamanho máximo por valor em metadados ou atributos de anexo (POC). */
    private static final int VALOR_TEXTO_MAXIMO_POC = 280;

    /** Chaves de metadado: apenas snake_case minúsculo ({@code canal_origem}, {@code id_externo}). */
    private static final Pattern CHAVE_METADADO_SNAKE_CASE =
        Pattern.compile("^[a-z][a-z0-9_]*$");

    /** Marcador de preenchimento automático proibido nesta POC (simulação de template vazado). */
    private static final String MARCADOR_RESERVADO_POC = "[[PREENCHA_AQUI]]";

    private PoliticaConteudoSolicitacaoPadronizada() {}

    public static void validar(final String descricao, final Map<String, String> metadados) {
        validar(descricao, metadados, List.of());
    }

    /**
     * Valida descrição principal, metadados e atributos dos anexos segundo regras da POC.
     */
    public static void validar(
        final String descricao,
        final Map<String, String> metadados,
        final List<Map<String, String>> atributosAnexos
    ) {
        validarDescricao(descricao);
        validarMetadados(metadados);
        if (atributosAnexos == null) {
            return;
        }
        for (final Map<String, String> map : atributosAnexos) {
            validarMetadados(map == null ? Map.of() : map);
        }
    }

    private static void validarDescricao(final String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ViolacaoDominioException("descrição é obrigatória");
        }
        final var t = texto.trim();
        if (t.length() < DESCRICAO_MINIMA_EXTRA_POC) {
            throw new ViolacaoDominioException(
                "descrição deve ter ao menos %d caracteres para a fila POC"
                    .formatted(DESCRICAO_MINIMA_EXTRA_POC));
        }
        if (t.contains(MARCADOR_RESERVADO_POC)) {
            throw new ViolacaoDominioException(
                "descrição não pode conter o marcador reservado da POC");
        }
    }

    private static void validarMetadados(final Map<String, String> metadados) {
        if (metadados == null || metadados.isEmpty()) {
            return;
        }
        for (final Map.Entry<String, String> e : metadados.entrySet()) {
            final var chave = e.getKey();
            final var valor = e.getValue() == null ? "" : e.getValue();
            if (chave != null && !chave.isBlank()) {
                if (!CHAVE_METADADO_SNAKE_CASE.matcher(chave).matches()) {
                    throw new ViolacaoDominioException(
                        "chaves de metadado devem estar em snake_case (letras minúsculas)");
                }
            }
            if (valor.length() > VALOR_TEXTO_MAXIMO_POC) {
                throw new ViolacaoDominioException(
                    "valor de metadado excede %d caracteres (limite POC)"
                        .formatted(VALOR_TEXTO_MAXIMO_POC));
            }
        }
    }
}
