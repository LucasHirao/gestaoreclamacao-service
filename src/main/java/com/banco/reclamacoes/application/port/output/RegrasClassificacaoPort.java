package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicao;
import java.util.List;

public interface RegrasClassificacaoPort {
    List<RegraClassificacaoDefinicao> regras();
}
