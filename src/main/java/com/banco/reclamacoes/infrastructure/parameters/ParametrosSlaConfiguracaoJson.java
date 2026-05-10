package com.banco.reclamacoes.infrastructure.parameters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class ParametrosSlaConfiguracaoJson {
    public int prazoPadraoDiasCorridos;
    public int diasAntesParaAlerta;
    public int limiteBuscaAlertas;
}
