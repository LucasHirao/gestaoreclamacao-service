package com.banco.reclamacoes.infrastructure.web.dto;

import java.util.List;

public record ResultadoClassificacaoRespostaDto(
    String categoriaPrincipal,
    List<CategoriaPontuacaoDto> categoriasCandidatas,
    int scorePrincipal,
    String confianca,
    List<String> justificativas,
    boolean requerRevisaoManual
) {}
