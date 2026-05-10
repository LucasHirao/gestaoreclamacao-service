package com.banco.reclamacoes.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ViolacaoDominioExceptionTest {

    @Test
    void preservaMensagem() {
        ViolacaoDominioException e = new ViolacaoDominioException("erro de domínio");

        assertThat(e.getMessage()).isEqualTo("erro de domínio");
    }
}
