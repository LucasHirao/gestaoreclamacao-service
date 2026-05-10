package com.banco.reclamacoes.infrastructure.messaging;

import com.banco.reclamacoes.application.port.output.IntegracaoPublicacaoComando;
import com.banco.reclamacoes.application.port.output.PublicadorIntegracaoPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsOperations;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Publicação de integração via {@link SqsOperations} (Spring Cloud AWS). */
@RequiredArgsConstructor
public class SqsPublicadorIntegracaoAdapter implements PublicadorIntegracaoPort {

    private static final Logger log = LoggerFactory.getLogger(SqsPublicadorIntegracaoAdapter.class);

    private final SqsOperations sqsOperations;
    private final ObjectMapper objectMapper;
    private final String filaUrlOuNome;

    @Override
    public void publicar(final IntegracaoPublicacaoComando comando) {
        try {
            final var payload =
                new MensagemIntegracaoPayload( comando.reclamacaoId().valor().toString(), comando.protocolo().valor(), comando.clienteId().valor(), comando.categoria().name(), comando.status().name(), comando.dataRecebimento().toString(), comando.correlationId().valor());
            final var corpo = objectMapper.writeValueAsString(payload);
            sqsOperations.send(opts -> opts.queue(filaUrlOuNome).payload(corpo));
            log.debug("Mensagem de integração enviada para fila {}", filaUrlOuNome);
        } catch (final Exception e) {
            throw new IllegalStateException("Falha ao publicar integração em SQS", e);
        }
    }
}
