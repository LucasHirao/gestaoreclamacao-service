package com.banco.reclamacoes.bdd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sns.SnsClient;

@Component
@Profile("bdd")
public class BddFronteirasSanidade {

    private static final Logger log = LoggerFactory.getLogger(BddFronteirasSanidade.class);

    private final DynamoDbClient dynamoDb;
    private final SnsClient sns;
    private final String tabela;
    private final String topicoArn;

    BddFronteirasSanidade(
        final DynamoDbClient dynamoDb,
        final SnsClient sns,
        @Value("${aws.bdd.dynamodb-table}") final String tabela,
        @Value("${aws.bdd.sns-topic-arn}") final String topicoArn) {
        this.dynamoDb = dynamoDb;
        this.sns = sns;
        this.tabela = tabela;
        this.topicoArn = topicoArn;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validarFronteirasInfraestrutura() {
        try {
            dynamoDb.describeTable(r -> r.tableName(tabela));
        } catch (final ResourceNotFoundException e) {
            throw new IllegalStateException(
                "Tabela DynamoDB '%s' inexistente no LocalStack — rode docker compose ou o init do LocalStack."
                    .formatted(tabela), e);
        }
        sns.publish(r -> r.topicArn(topicoArn).message("{\"bddFronteira\":\"sanidade\"}"));
        log.info(
            "BDD: esquema SQL validado (Flyway/JPA), fila SQS configurada, DynamoDB e SNS respondendo no LocalStack.");
    }
}
