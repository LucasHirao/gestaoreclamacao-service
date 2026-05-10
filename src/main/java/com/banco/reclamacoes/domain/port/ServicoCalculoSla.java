package com.banco.reclamacoes.domain.port;

import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.banco.reclamacoes.domain.valueobject.Sla;
import java.time.Instant;

/** Política configurável de prazos (deadline e alerta) para o SLA da reclamação. */
public interface ServicoCalculoSla {

    Sla calcular(Instant dataRecebimento, ParametrosSla parametros);
}
