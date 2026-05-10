package com.banco.reclamacoes.domain.valueobject;

import com.banco.reclamacoes.domain.exception.ViolacaoDominioException;
import java.util.Objects;

public final class DescricaoReclamacao {

    private static final int TAMANHO_MINIMO = 10;

    private final String texto;

    private DescricaoReclamacao(final String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ViolacaoDominioException("descrição da reclamação é obrigatória");
        }
        String t = texto.trim();
        if (t.length() < TAMANHO_MINIMO) {
            throw new ViolacaoDominioException(
                "descrição deve ter ao menos %d caracteres".formatted(TAMANHO_MINIMO));
        }
        this.texto = t;
    }

    public static DescricaoReclamacao de(final String texto) {
        return new DescricaoReclamacao(texto);
    }

    public String texto() {
        return texto;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DescricaoReclamacao that = (DescricaoReclamacao) o;
        return texto.equals(that.texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto);
    }

    @Override
    public String toString() {
        return texto;
    }
}
