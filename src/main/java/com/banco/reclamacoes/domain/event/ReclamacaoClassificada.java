package com.banco.reclamacoes.domain.event;

import com.banco.reclamacoes.domain.model.ConfiancaClassificacao;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.time.Instant;

public record ReclamacaoClassificada(
    ReclamacaoId reclamacaoId,
    CategoriaReclamacao categoriaPrincipal,
    ConfiancaClassificacao confiancaClassificacao,
    Instant ocorridoEm
) implements EventoDominio {
}
