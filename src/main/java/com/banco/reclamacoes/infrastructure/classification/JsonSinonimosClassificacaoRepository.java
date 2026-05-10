package com.banco.reclamacoes.infrastructure.classification;

import com.banco.reclamacoes.application.port.output.SinonimosClassificacaoPort;
import com.banco.reclamacoes.domain.service.NormalizacaoTextual;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class JsonSinonimosClassificacaoRepository implements SinonimosClassificacaoPort {

    private final ObjectMapper objectMapper;
    private final Resource arquivo;

    private Map<String, String> variantesParaCanonico = Map.of();

    public JsonSinonimosClassificacaoRepository(
        final ObjectMapper objectMapper,
        @Value("${app.classification.sinonimos}") final Resource arquivo
    ) {
        this.objectMapper = objectMapper;
        this.arquivo = arquivo;
    }

    @PostConstruct
    void carregar() throws IOException {
        try ( var stream = arquivo.getInputStream()) {
            final var bruto =
                objectMapper.readValue(stream, new TypeReference<Map<String, List<String>>>() {});
            final var construido = new HashMap<String, String>();
            for (final var entrada : bruto.entrySet()) {
                final var canonicoNormalizado =
                    NormalizacaoTextual.normalizar(entrada.getKey());
                construido.put(canonicoNormalizado, canonicoNormalizado);
                for (final var variante : entrada.getValue()) {
                    construido.put(NormalizacaoTextual.normalizar(variante), canonicoNormalizado);
                }
            }
            variantesParaCanonico = Collections.unmodifiableMap(construido);
        }
    }

    @Override
    public Map<String, String> variantesNormalizadasParaCanonico() {
        return variantesParaCanonico;
    }
}
