package com.banco.reclamacoes.infrastructure.web.dto;

import java.util.Map;

public record AnexoRespostaDto(String referencia, String tipoMime, Map<String, String> atributos) {}
