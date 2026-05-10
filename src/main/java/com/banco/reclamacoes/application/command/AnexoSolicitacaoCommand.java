package com.banco.reclamacoes.application.command;

import java.util.Map;

public record AnexoSolicitacaoCommand(String referencia, String tipoMime, Map<String, String> atributos) {}
