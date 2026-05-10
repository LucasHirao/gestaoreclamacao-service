package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class ParametrosSla {

    private final int prazoPadraoDiasCorridos;
    private final int diasAntesParaAlerta;
    private final int limiteBuscaAlertas;

    private ParametrosSla( int prazoPadraoDiasCorridos, int diasAntesParaAlerta, int limiteBuscaAlertas) {
        if (prazoPadraoDiasCorridos <= 0) {
            throw new ViolacaoDominioException("prazo padrão de SLA deve ser positivo");
        }
        if (diasAntesParaAlerta < 0) {
            throw new ViolacaoDominioException("dias antes para alerta não pode ser negativo");
        }
        if (limiteBuscaAlertas <= 0) {
            throw new ViolacaoDominioException("limite de busca de alertas deve ser positivo");
        }
        this.prazoPadraoDiasCorridos = prazoPadraoDiasCorridos;
        this.diasAntesParaAlerta = diasAntesParaAlerta;
        this.limiteBuscaAlertas = limiteBuscaAlertas;
    }

    public static ParametrosSla of( int prazoPadraoDiasCorridos, int diasAntesParaAlerta, int limiteBuscaAlertas) {
        return new ParametrosSla(prazoPadraoDiasCorridos, diasAntesParaAlerta, limiteBuscaAlertas);
    }

    public int prazoPadraoDiasCorridos() {
        return prazoPadraoDiasCorridos;
    }

    public int diasAntesParaAlerta() {
        return diasAntesParaAlerta;
    }

    public int limiteBuscaAlertas() {
        return limiteBuscaAlertas;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParametrosSla that = (ParametrosSla) o;
        return prazoPadraoDiasCorridos == that.prazoPadraoDiasCorridos
            && diasAntesParaAlerta == that.diasAntesParaAlerta
            && limiteBuscaAlertas == that.limiteBuscaAlertas;
    }

    @Override
    public int hashCode() {
        return Objects.hash(prazoPadraoDiasCorridos, diasAntesParaAlerta, limiteBuscaAlertas);
    }
}
