package com.banco.reclamacoes.infrastructure.web.dto.dev;

import java.util.Map;

public record AnexoInboundDto(String referencia, String tipoMime, Map<String, String> atributos) {}
