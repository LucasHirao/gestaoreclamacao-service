package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class Protocolo {

    private final String valor;

    private Protocolo(final String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ViolacaoDominioException("protocolo não pode ser vazio");
        }
        this.valor = valor.trim();
    }

    public static Protocolo de(final String valor) {
        return new Protocolo(valor);
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
        Protocolo protocolo = (Protocolo) o;
        return valor.equals(protocolo.valor);
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
