package com.banco.reclamacoes.infrastructure.classification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
@Getter
class RegraDocumentoJson {

    private List<RegraDocumentoEntradaJson> regras = new ArrayList<>();
    private List<RegraDocumentoCompostaJson> compostas = new ArrayList<>();
}
