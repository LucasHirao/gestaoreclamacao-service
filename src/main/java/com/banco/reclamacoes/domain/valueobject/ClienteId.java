package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class ClienteId {

    private final String valor;

    private ClienteId(final String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ViolacaoDominioException("clienteId não pode ser vazio");
        }
        this.valor = valor.trim();
    }

    public static ClienteId de(final String valor) {
        return new ClienteId(valor);
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
        ClienteId clienteId = (ClienteId) o;
        return valor.equals(clienteId.valor);
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
