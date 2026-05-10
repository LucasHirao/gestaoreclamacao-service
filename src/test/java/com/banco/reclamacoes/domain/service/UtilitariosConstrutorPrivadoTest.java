package com.banco.reclamacoes.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.Test;

class UtilitariosConstrutorPrivadoTest {

    @Test
    void classesUtilitariasExpoeConstrutorPrivadoPadrao() {
        assertThatCode(
                () -> {
                    for (final Class<?> tipo :
                        List.of(NormalizacaoTextual.class)) {
                        final var ctor = tipo.getDeclaredConstructor();
                        ctor.setAccessible(true);
                        ctor.newInstance();
                    }
                })
            .doesNotThrowAnyException();
    }
}
