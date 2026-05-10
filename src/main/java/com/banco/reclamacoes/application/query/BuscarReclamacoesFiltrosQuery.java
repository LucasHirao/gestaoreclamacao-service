package com.banco.reclamacoes.application.query;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;

public record BuscarReclamacoesFiltrosQuery(
    String clienteId,
    StatusReclamacao status,
    CategoriaReclamacao categoria,
    int pagina,
    int tamanho
) {}
