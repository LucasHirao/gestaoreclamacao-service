# Gestão de Reclamações (MVP)

Serviço em Java 21 + Spring Boot seguindo DDD tático e portas/adaptadores. O núcleo de domínio permanece livre de Spring/JPA/AWS/Cucumber. Integrações AWS (SQS, SNS, DynamoDB) usam **[Spring Cloud AWS](https://docs.awspring.io/spring-cloud-aws/docs/3.4.0/reference/html/index.html)** (`io.awspring.cloud`): auto-configuração de clientes, `SqsTemplate`/`SqsOperations` para envio e `@SqsListener` para consumo.

## Executar testes

Requer **Docker** e **Compose v2** com suporte a `docker compose up --wait` (o `mvn verify` sobe automaticamente os serviços `postgres` e `localstack` antes do Cucumber).

```bash
mvn verify                         # Surefire (unitários) → Docker (Postgres + LocalStack: SQS, SNS, DynamoDB) → Cucumber/BDD → JaCoCo (relatório + gate ≥90%) → PIT (mutação ≥90% no escopo do pom)
mvn verify -DskipIntegrationInfra=true   # apenas unitários + JaCoCo + PIT (sem Docker nem BDD)
mvn test-compile exec:java@bdd     # apenas Cucumber manual (com Docker já rodando ou com infra ignorada através de properties)
mvn test-compile exec:java@jmh-main   # benchmarks JMH (Classificador…)
```

O perfil Spring `bdd` (Cucumber) usa **PostgreSQL** local (Flyway + JPA validate), **SQS** (`integracao-reclamacoes`) e ainda faz *smoke* de **SNS** e **DynamoDB** no LocalStack na subida da aplicação de teste.

## Conventional Commits

Use mensagens curtas no formato `<tipo>(escopo opcional)!: descrição`, por exemplo `feat(classificação): aumenta cobertura de sinônimos`. Commits devem permanecer pequenos e coesos (o histórico Git local depende da sua política de equipe).

## Debug / demonstração local

1. Perfil **`local`** ou **`dev`**: **`spring.cloud.aws.endpoint`** apontado ao LocalStack, credenciais `test/test`, **`spring.cloud.aws.sqs.queue-not-found-strategy: CREATE`** (filas criadas pelo framework). Expõe ainda `@Profile({"local","dev"})`: `POST /dev/simular-solicitacao-padronizada` e `POST /dev/classificar-texto`.
2. Script exemplo: `./scripts/demo-debug-local.sh` (variável `BASE` opcional).

## Compose: Prometheus + Grafana + LocalStack (SQS/SNS/Dynamo tipo Floci/Localstack)

```bash
docker compose up -d prometheus grafana localstack postgres
```

- Prometheus: http://localhost:9090 — alvo em `infra/prometheus/prometheus.yml` (`host.docker.internal:8080`; ajuste se necessário no Linux sem Docker Desktop).
- Grafana: http://localhost:3000 — admin/admin — datasource Prometheus provisionado.
- LocalStack (`AWS_ENDPOINT_URL` padrão `http://localhost.localstack.cloud:4566`): filas SQS, tópico SNS `integracao-eventos`, tabela DynamoDB `bdd-fronteira-sanidade` — criados por `scripts/localstack-init.sh`.

Consulte também `docs/REVISAO-25-PONTOS.md` para a auditoria solicitada pelo checklist arquitetural.
