package com.banco.reclamacoes.domain.service;

import com.banco.reclamacoes.domain.model.RegraClassificacaoDefinicao;
import java.util.Objects;
import java.util.regex.Pattern;

record RegraRegexCompilada(Pattern pattern, RegraClassificacaoDefinicao definicao) {
    RegraRegexCompilada {
        Objects.requireNonNull(pattern);
        Objects.requireNonNull(definicao);
    }
}
