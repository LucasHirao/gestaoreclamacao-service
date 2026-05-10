package com.banco.reclamacoes.infrastructure.config;

import com.banco.reclamacoes.application.port.output.PublicadorIntegracaoPort;
import com.banco.reclamacoes.infrastructure.messaging.LoggingPublicadorIntegracaoAdapter;
import com.banco.reclamacoes.infrastructure.messaging.SqsPublicadorIntegracaoAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MensageriaIntegracaoConfiguracao {

    /**
     * Ambiente tipo Floci/LocalStack/BDD: ative o perfil {@code bdd} (ver {@code application-bdd.yml}) para publicar
     * na fila real via Spring Cloud AWS.
     */
    @Bean
    @Profile("bdd")
    @ConditionalOnProperty(name = "aws.sqs.integracao-queue")
    PublicadorIntegracaoPort publicadorIntegracaoSqs(
        final SqsOperations sqsOperations,
        final ObjectMapper objectMapper,
        @Value("${aws.sqs.integracao-queue}") final String fila) {
        return new SqsPublicadorIntegracaoAdapter(sqsOperations, objectMapper, fila);
    }

    @Bean
    @ConditionalOnMissingBean(PublicadorIntegracaoPort.class)
    PublicadorIntegracaoPort publicadorIntegracaoMemoria(final ObjectMapper mapper) {
        return new LoggingPublicadorIntegracaoAdapter(mapper);
    }
}
