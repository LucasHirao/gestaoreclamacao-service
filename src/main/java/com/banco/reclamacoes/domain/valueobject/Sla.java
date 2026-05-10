package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class Sla {

    private final PrazoSla deadline;
    private final PrazoSla alerta;

    private Sla(final PrazoSla deadline, final PrazoSla alerta) {
        if (deadline == null || alerta == null) {
            throw new ViolacaoDominioException("SLA incompleto");
        }
        if (alerta.instant().isAfter(deadline.instant())) {
            throw new ViolacaoDominioException("alertAt não pode ser posterior ao deadlineAt");
        }
        this.deadline = deadline;
        this.alerta = alerta;
    }

    public static Sla of(final PrazoSla deadline, final PrazoSla alerta) {
        return new Sla(deadline, alerta);
    }

    public PrazoSla deadline() {
        return deadline;
    }

    public PrazoSla alerta() {
        return alerta;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Sla sla = (Sla) o;
        return deadline.equals(sla.deadline) && alerta.equals(sla.alerta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deadline, alerta);
    }
}
