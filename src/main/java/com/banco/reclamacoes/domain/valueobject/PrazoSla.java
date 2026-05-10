package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.time.Instant;
import java.util.Objects;

public final class PrazoSla {

    private final Instant instant;

    private PrazoSla(final Instant instant) {
        if (instant == null) {
            throw new ViolacaoDominioException("instante SLA inválido");
        }
        this.instant = instant;
    }

    public static PrazoSla de(final Instant instant) {
        return new PrazoSla(instant);
    }

    public Instant instant() {
        return instant;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrazoSla prazoSla = (PrazoSla) o;
        return instant.equals(prazoSla.instant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instant);
    }
}
