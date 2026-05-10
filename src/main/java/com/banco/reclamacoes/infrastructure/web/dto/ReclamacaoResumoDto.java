package com.banco.reclamacoes.infrastructure.web.dto;

import java.time.Instant;

public record ReclamacaoResumoDto(
    String protocolo,
    String clienteId,
    String status,
    String categoriaPrincipal,
    String confiancaClassificacao,
    Instant deadlineAt,
    Instant alertAt,
    boolean alertaEmitido,
    Instant dataRecebimento
) {}
