# Revisão arquitetural (25 pontos)

| # | Pergunta | Status | Evidência |
|---|----------|--------|-----------|
| 1 | Domínio livre de Spring, JPA, AWS, Floci, Cucumber? | **Sim** | `grep` sobre `domain/` sem imports externos; apenas JDK e pacotes internos. |
| 2 | Regras de negócio em Controllers/Consumers/Repositories? | **Não** — orquestração fina apenas | Controllers delegam use cases; repositório apenas persistência/consultas; motor de decisão está no domínio/políticas e no classificador (infraestrutura de regras configuráveis). |
| 3 | Agregado `Reclamacao` rico? | **Sim** | `criar`, `classificar`, `marcarAlertaSlaEmitido`, `registrarHistorico`, eventos de domínio, sem setters públicos livres. |
| 4 | Classificador: if/else gigante vs configurável? | **Configurável** | Regras em JSON/YAML-like via portas (`RegrasClassificacaoPort`, compostas); `switch` apenas por tipo de termo ao indexar (estrutura, não lista fixa negócio). |
| 5 | Sinônimos? | **Sim** | `SinonimosClassificacaoPort` / `sinonimos-classificacao.json`. |
| 6 | Índice invertido termos simples? | **Sim** | `indicePorTermo` (`Map<String, List<Regra>>`) em `ClassificadorPorRegrasPonderadas`. |
| 7 | Separação palavra/frase/composta (e regex)? | **Sim** | PALAVRA no índice ou lista de frases; FRASE/REGEX dedicados; compostas por `NormalizacaoTextual.contemTodosTermos`. |
| 8 | Score por categoria? | **Sim** | Acumulador `EnumMap` + conversão para `ScoreClassificacao`. |
| 9 | Justificativa? | **Sim** | Lista preenchida em `somar` com texto da regra. |
| 10 | Cálculo de confiança? | **Sim** | `PoliticaConfiancaClassificacao`. |
| 11 | Solicitação padronizada sem CPF/CNPJ? | **Regra no domínio** | `PoliticaConteudoSolicitacaoPadronizada` valida texto/metadados/anexos contra máscaras e chaves sensíveis. |
| 12 | Idempotência `solicitacaoId` / `idempotencyKey`? | **Sim** | `ProcessarSolicitacaoPadronizadaUseCase` antes de criar. |
| 13 | SLA no domínio? | **Sim** | VO `Sla`, serviço `CalculadoraSla`. |
| 14 | SLA parametrizável? | **Sim** | `ParametrosSlaPort` + JSON (`sla-parametros.json`). |
| 15 | Regras de classificação parametrizáveis? | **Sim** | JSON `regras-classificacao.json` + portas de leitura. |
| 16 | Outbox evitado no MVP? | **Sim** | Adaptador apenas log quando sem SQS; comentários explícitos em `LoggingPublicadorIntegracaoAdapter`. |
| 17 | Testes AAA? | **Parcial/objectivo sim** | Vários testes seguem Arrange/Act/Assert; exemplo explícito em `PoliticaConteudoSolicitacaoPadronizadaTest`. |
| 18 | Cobertura unitária JaCoCo >90%? | **Gate no bundle recortado** | `jacoco-maven-plugin` conta só `domain` + `application/usecase` + `application/classification`, excluindo `domain/valueobject`. `mvn verify` deve cumprir 90%; relatório em `target/site/jacoco/`. |
| 19 | PIT ≥90%? | **Configurado** | `mutationThreshold` e `coverageThreshold` 90 no `pom.xml`; executar `mvn pitest:mutationCoverage`. |
| 20 | BDD Cucumber? | **Sim** | `mvn test-compile exec:java@bdd`, feature em PT, glue em `com.banco.reclamacoes.bdd`. |
| 21 | Integração local Floci-like? | **Sim (BDD + LocalStack/Floci)** | `docker compose up localstack`; BDD usa perfis `test`+`bdd` e `application-bdd.yml` (`AWS_ENDPOINT_URL` + `SQS_INTEGRACAO`). |
| 22 | docker-compose Prometheus + Grafana? | **Sim** | `docker-compose.yml` + `infra/prometheus` + provisionamento Grafana. |
| 23 | Debug simples? | **Sim** | profiles `local/dev`, endpoints `/dev/...`, `scripts/demo-debug-local.sh`, actuator `prometheus`. |
| 24 | `/dev` só local/dev? | **Sim** | `@Profile({"local","dev"})` em `AmbienteDesenvolvimentoController`. |
| 25 | Commits pequenos + Conventional Commits? | **Processo/Git** | Não automatizado pelo repositório; ver secção README. |
