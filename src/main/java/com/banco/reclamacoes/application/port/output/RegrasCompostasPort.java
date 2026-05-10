package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.model.RegraCompostaDefinicao;
import java.util.List;

public interface RegrasCompostasPort {
    List<RegraCompostaDefinicao> regrasCompostas();
}
