package com.banco.reclamacoes.infrastructure.web.dto.dev;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClassificarTextoRequisicaoDto(
    @Schema(example = "não reconheço uma compra no meu cartão") String texto
) {}
