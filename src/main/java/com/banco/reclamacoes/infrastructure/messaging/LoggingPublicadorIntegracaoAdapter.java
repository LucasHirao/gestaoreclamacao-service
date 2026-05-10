package com.banco.reclamacoes.infrastructure.messaging;

import com.banco.reclamacoes.application.port.output.IntegracaoPublicacaoComando;
import com.banco.reclamacoes.application.port.output.PublicadorIntegracaoPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MVP deliberadamente sem Outbox: a transação principal persiste o agregado e, em seguida, este
 * adaptador apenas registra a intenção de integração nos logs quando não há fila SQS configurada.
 *
 * <p>Outbox/transacionalidade com broker fica como evolução explícita; com SQS ativo usa-se {@code
 * SqsPublicadorIntegracaoAdapter}, ainda assim sem tabela outbox neste projeto.</p>
 */
@RequiredArgsConstructor
public class LoggingPublicadorIntegracaoAdapter implements PublicadorIntegracaoPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingPublicadorIntegracaoAdapter.class);

    private final ObjectMapper objectMapper;

    @Override
    public void publicar(final IntegracaoPublicacaoComando comando) {
        try {
            final var payload =
                new MensagemIntegracaoPayload( comando.reclamacaoId().valor().toString(), comando.protocolo().valor(), comando.clienteId().valor(), comando.categoria().name(), comando.status().name(), comando.dataRecebimento().toString(), comando.correlationId().valor());
            log.info("INTEGRACAO_PUBLICADA_BODY={}", objectMapper.writeValueAsString(payload));
        } catch (final Exception e) {
            throw new IllegalStateException("Erro ao preparar mensagem fake de integração", e);
        }
    }
}
