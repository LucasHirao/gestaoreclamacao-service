package com.banco.reclamacoes.domain.model;

import java.time.Instant;

public record HistoricoInterno(String tipo, String detalhe, Instant ocorridoEm) {}
