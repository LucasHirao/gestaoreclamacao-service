package com.banco.reclamacoes.infrastructure.parameters;

import com.banco.reclamacoes.application.port.output.ParametrosSlaPort;
import com.banco.reclamacoes.domain.valueobject.ParametrosSla;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class JsonParametrosSlaRepository implements ParametrosSlaPort {

    private final ObjectMapper objectMapper;
    private final Resource arquivo;

    private ParametrosSla parametros;

    public JsonParametrosSlaRepository(
        final ObjectMapper objectMapper,
        @Value("${app.sla.parametros}") final Resource arquivo) {
        this.objectMapper = objectMapper;
        this.arquivo = arquivo;
    }

    @PostConstruct
    void inicializar() throws IOException {
        try (InputStream stream = arquivo.getInputStream()) {
            ParametrosSlaConfiguracaoJson json = objectMapper.readValue(stream, ParametrosSlaConfiguracaoJson.class);
            parametros =
                ParametrosSla.of(
                    json.prazoPadraoDiasCorridos,
                    json.diasAntesParaAlerta,
                    json.limiteBuscaAlertas);
        }
    }

    @Override
    public ParametrosSla carregar() {
        return parametros;
    }

}
