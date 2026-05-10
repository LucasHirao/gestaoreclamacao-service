package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class CorrelationId {

    private final String valor;

    private CorrelationId(final String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ViolacaoDominioException("correlationId não pode ser vazio");
        }
        this.valor = valor.trim();
    }

    public static CorrelationId de(final String valor) {
        return new CorrelationId(valor);
    }

    public String valor() {
        return valor;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CorrelationId that = (CorrelationId) o;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
