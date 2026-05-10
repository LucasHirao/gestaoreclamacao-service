package com.banco.reclamacoes.infrastructure.domain;

import com.banco.reclamacoes.domain.port.GeradorProtocoloServico;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class GeradorProtocoloAleatorio implements GeradorProtocoloServico {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    @Override
    public Protocolo gerarParaDataRecebimento(final Instant dataRecebimento) {
        final String data = DATA.format(dataRecebimento);
        final int sufixo = ThreadLocalRandom.current().nextInt(10_000, 99_999);
        return Protocolo.de("REC-" + data + "-" + sufixo);
    }
}
