package com.banco.reclamacoes.infrastructure.web.dto.dev;

import com.banco.reclamacoes.domain.valueobject.ResultadoClassificacao;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ClassificarTextoRespostaDto(
    String categoriaPrincipal,
    List<String> categoriasCandidatas,
    String confianca,
    List<String> justificativas
) {
    public static ClassificarTextoRespostaDto deduzido(final ResultadoClassificacao resultado) {
        final var candidatas =
            resultado.categoriasCandidatas().stream()
                .map(c -> "%s (%d)".formatted(c.categoria().name(), c.score().pontos()))
                .toList();
        return new ClassificarTextoRespostaDto(
            resultado.categoriaPrincipal().name(),
            candidatas,
            resultado.confianca().name(),
            List.copyOf(resultado.justificativas()));
    }
}
