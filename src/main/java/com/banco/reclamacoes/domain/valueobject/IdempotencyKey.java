package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class IdempotencyKey {

    private final String valor;

    private IdempotencyKey(final String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ViolacaoDominioException("idempotencyKey não pode ser vazio");
        }
        this.valor = valor.trim();
    }

    public static IdempotencyKey de(final String valor) {
        return new IdempotencyKey(valor);
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
        IdempotencyKey that = (IdempotencyKey) o;
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
