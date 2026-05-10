package com.banco.reclamacoes.cucumber;

import io.cucumber.core.cli.Main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ponto de entrada para executar cenários Cucumber pela CLI (equivale ao {@link Main} usado pelo Maven na fase {@code bdd}).
 *
 * <p>Requer infraestrutura do perfil {@code bdd} (PostgreSQL e LocalStack), por exemplo {@code docker compose up -d postgres
 * localstack} na raiz do projeto. Só Postgres não basta: sem LocalStack na porta 4566, o AWS SDK falha (ex.: {@code localhost.localstack.cloud:4566 Connection refused} em
 * {@link com.banco.reclamacoes.bdd.BddFronteirasSanidade}).
 *
 * <p>Rodando pelo IntelliJ (botão Run em {@code main}): o IDE não executa o Maven que sobe os contêineres — suba {@code postgres} e {@code localstack} no Compose antes.
 *
 * <h2>Todas as features em {@code classpath:features}</h2>
 *
 * <pre>mvn test-compile exec:java@run-cucumber</pre>
 *
 * <h2>Uma única feature</h2>
 *
 * <pre>
 * mvn test-compile exec:java@run-cucumber "-Dexec.args=classpath:features/processamento.feature"
 * </pre>
 *
 * <h2>Um único cenário (regex sobre o nome)</h2>
 *
 * <pre>
 * mvn test-compile exec:java@run-cucumber "-Dexec.args=classpath:features/processamento.feature --name ^Criar.reclama"
 * </pre>
 */
public final class RunCucumberTests {

    private static final String GLUE_PACOTE = "com.banco.reclamacoes.bdd";

    private RunCucumberTests() {}

    public static void main(String[] args) {
        List<String> cmd = new ArrayList<>();
        cmd.add("--glue");
        cmd.add(GLUE_PACOTE);
        cmd.add("--plugin");
        cmd.add("pretty");
        cmd.add("--plugin");
        cmd.add("summary");

        String[] usuario = args == null ? new String[0] : args;

        if (usuario.length == 0) {
            cmd.add("classpath:features");
        } else if (usuario[0].startsWith("--")) {
            cmd.add("classpath:features");
            cmd.addAll(Arrays.asList(usuario));
        } else {
            cmd.addAll(Arrays.asList(usuario));
        }

        Main.main(cmd.toArray(String[]::new));
    }
}
