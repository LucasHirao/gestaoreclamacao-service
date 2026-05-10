package com.banco.reclamacoes.domain.port;

import com.banco.reclamacoes.domain.valueobject.Protocolo;
import java.time.Instant;

/** Abstrai a regra de negócio que produz o protocolo oficial a partir da data de recebimento. */
public interface GeradorProtocoloServico {

    Protocolo gerarParaDataRecebimento(Instant dataRecebimento);
}
