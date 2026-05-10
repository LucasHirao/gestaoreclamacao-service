package com.banco.reclamacoes.infrastructure.web.dto;

import java.time.Instant;

public record HistoricoLinhaDto(String tipo, String detalhe, Instant ocorridoEm) {}
