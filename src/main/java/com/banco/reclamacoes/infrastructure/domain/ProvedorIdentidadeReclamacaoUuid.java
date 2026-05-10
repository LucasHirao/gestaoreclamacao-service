package com.banco.reclamacoes.infrastructure.domain;

import com.banco.reclamacoes.domain.port.ProvedorIdentidadeReclamacao;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProvedorIdentidadeReclamacaoUuid implements ProvedorIdentidadeReclamacao {

    @Override
    public ReclamacaoId novaIdentidade() {
        return ReclamacaoId.de(UUID.randomUUID());
    }
}
