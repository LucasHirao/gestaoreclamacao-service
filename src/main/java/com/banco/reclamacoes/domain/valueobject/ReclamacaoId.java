package com.banco.reclamacoes.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class ReclamacaoId {

    private final UUID valor;

    private ReclamacaoId(final UUID valor) {
        this.valor = Objects.requireNonNull(valor, "reclamacaoId");
    }

    public static ReclamacaoId gerar() {
        return new ReclamacaoId(UUID.randomUUID());
    }

    public static ReclamacaoId de(final UUID uuid) {
        return new ReclamacaoId(uuid);
    }

    public UUID valor() {
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
        ReclamacaoId that = (ReclamacaoId) o;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
