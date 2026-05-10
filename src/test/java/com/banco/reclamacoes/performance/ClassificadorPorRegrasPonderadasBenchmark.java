package com.banco.reclamacoes.performance;

import com.banco.reclamacoes.domain.service.ClassificadorPorRegrasPonderadas;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.infrastructure.classification.JsonRegrasClassificacaoRepository;
import com.banco.reclamacoes.infrastructure.classification.JsonSinonimosClassificacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.core.io.ClassPathResource;

/**
 * Mede o custo médio de classificação com as regras JSON reais carregadas do classpath.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(1)
public class ClassificadorPorRegrasPonderadasBenchmark {

    private ClassificadorPorRegrasPonderadas classificador;
    private List<DescricaoReclamacao> amostras;

    @Setup
    public void preparar() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        JsonSinonimosClassificacaoRepository sinonimos =
            new JsonSinonimosClassificacaoRepository(
                    mapper, new ClassPathResource("classificacao/sinonimos-classificacao.json"));
        invocarSemArg(sinonimos, "carregar");

        JsonRegrasClassificacaoRepository regras =
            new JsonRegrasClassificacaoRepository(
                    mapper, new ClassPathResource("classificacao/regras-classificacao.json"));
        invocarSemArg(regras, "carregar");

        classificador =
            ClassificadorPorRegrasPonderadas.montar(
                    regras.regras(), regras.regrasCompostas(), sinonimos.variantesNormalizadasParaCanonico());

        amostras = new ArrayList<>();
        amostras.add(DescricaoReclamacao.de("não reconheço uma compra no meu cartão"));
        amostras.add(
            DescricaoReclamacao.de(
                "Tive cobrança estranha na minha fatura mas também uso muito o cartão nos últimos dias."));
        amostras.add(
            DescricaoReclamacao.de(
                "Cliente relata problema sério com tarifas e extrato que não batem com o uso real."));
    }

    private static void invocarSemArg(final Object alvo, final String metodo) throws Exception {
        final var m = alvo.getClass().getDeclaredMethod(metodo);
        m.setAccessible(true);
        m.invoke(alvo);
    }

    @Benchmark
    public void classificarCenariosReais(final Blackhole hole) {
        for (final var d : amostras) {
            hole.consume(classificador.classificar(d).categoriaPrincipal());
        }
    }
}
