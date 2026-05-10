package com.banco.reclamacoes.infrastructure.classification;

import com.banco.reclamacoes.application.port.output.RegrasClassificacaoPort;
import com.banco.reclamacoes.application.port.output.RegrasCompostasPort;
import com.banco.reclamacoes.domain.model.CriticidadeRegra;
import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicao;
import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicaoDados;
import com.banco.reclamacoes.domain.model.RegraCompostaDefinicao;
import com.banco.reclamacoes.domain.model.RegraCompostaDefinicaoDados;
import com.banco.reclamacoes.domain.service.NormalizacaoTextual;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class JsonRegrasClassificacaoRepository implements RegrasClassificacaoPort, RegrasCompostasPort {

    private final ObjectMapper objectMapper;
    private final Resource arquivo;

    private List<RegraClassificacaoDefinicao> regras = List.of();
    private List<RegraCompostaDefinicao> compostas = List.of();

    public JsonRegrasClassificacaoRepository(
        final ObjectMapper objectMapper,
        @Value("${app.classification.regras}") final Resource arquivo
    ) {
        this.objectMapper = objectMapper;
        this.arquivo = arquivo;
    }

    @PostConstruct
    void carregar() throws IOException {
        try ( var stream = arquivo.getInputStream()) {
            final var documento = objectMapper.readValue(stream, RegraDocumentoJson.class);
            final var carregadas = new ArrayList<RegraClassificacaoDefinicao>();
            for (final var entrada : documento.getRegras()) {
                final var termoNormalizado = NormalizacaoTextual.normalizar(entrada.termo);
                final var dadosRegra =
                    RegraClassificacaoDefinicaoDados.builder()
                        .categoria(entrada.categoria)
                        .termoNormalizado(termoNormalizado)
                        .peso(entrada.peso)
                        .tipo(entrada.tipo)
                        .criticidade(Objects.requireNonNullElse(entrada.criticidade, CriticidadeRegra.MEDIA))
                        .justificativa(entrada.justificativa == null ? "" : entrada.justificativa)
                        .build();
                carregadas.add(RegraClassificacaoDefinicao.criar(dadosRegra));
            }
            regras = Collections.unmodifiableList(carregadas);

            final var compostasCarregadas = new ArrayList<RegraCompostaDefinicao>();
            for (final var comp : documento.getCompostas()) {
                final var termos = new ArrayList<String>();
                for (final var termo : Optional.ofNullable(comp.termosObrigatorios).orElseGet(List::of)) {
                    termos.add(NormalizacaoTextual.normalizar(termo));
                }
                final var dadosComposta =
                    RegraCompostaDefinicaoDados.builder()
                        .categoria(comp.categoria)
                        .termosObrigatoriosNormalizados(termos)
                        .pesoExtra(comp.pesoExtra)
                        .justificativa(comp.justificativa == null ? "" : comp.justificativa)
                        .build();
                compostasCarregadas.add(RegraCompostaDefinicao.criar(dadosComposta));
            }
            compostas = Collections.unmodifiableList(compostasCarregadas);
        }
    }

    @Override
    public List<RegraClassificacaoDefinicao> regras() {
        return regras;
    }

    @Override
    public List<RegraCompostaDefinicao> regrasCompostas() {
        return compostas;
    }
}
