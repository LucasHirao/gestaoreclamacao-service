package com.banco.reclamacoes.infrastructure.messaging;

import com.banco.reclamacoes.application.port.input.RegistrarSolicitacaoPadronizadaPort;
import com.banco.reclamacoes.infrastructure.web.dto.dev.SolicitacaoPadronizadaInboundDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "aws.sqs", name = "standardized-request-queue")
public class SolicitacaoPadronizadaSqsSubscriber {

    private static final Logger log = LoggerFactory.getLogger(SolicitacaoPadronizadaSqsSubscriber.class);

    private final ObjectMapper objectMapper;
    private final RegistrarSolicitacaoPadronizadaPort registrarPort;

    SolicitacaoPadronizadaSqsSubscriber(
            final ObjectMapper objectMapper, final RegistrarSolicitacaoPadronizadaPort registrarPort) {
        this.objectMapper = objectMapper;
        this.registrarPort = registrarPort;
    }

    /** Spring Cloud AWS: consumo gerido pelo listener (long poll, ack quando o método termina sem exceção). */
    @SqsListener("${aws.sqs.standardized-request-queue}")
    public void aoReceberCorpoPadronizado(final String corpoJson) throws Exception {
        try {
            final var dto =
                objectMapper.readValue(corpoJson, SolicitacaoPadronizadaInboundDto.class);
            registrarPort.processar(dto.paraComando());
        } catch (final Exception e) {
            log.error("Erro ao consumir Solicitação Padronizada da fila SQS", e);
            throw e;
        }
    }
}
