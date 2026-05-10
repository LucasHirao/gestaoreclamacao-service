package com.banco.reclamacoes.domain.valueobject;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MetadadoAnexo {

    private final String referencia;
    private final String tipoMime;
    private final Map<String, String> atributos;

    public MetadadoAnexo(final String referencia, final String tipoMime, final Map<String, String> atributos) {
        this.referencia = referencia == null ? "" : referencia;
        this.tipoMime = tipoMime == null ? "" : tipoMime;
        this.atributos = atributos == null ? Map.of() : Map.copyOf(atributos);
    }

    public String referencia() {
        return referencia;
    }

    public String tipoMime() {
        return tipoMime;
    }

    public Map<String, String> atributos() {
        return atributos;
    }

    public Optional<String> atributo(final String chave) {
        return Optional.ofNullable(atributos.get(chave));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadadoAnexo that = (MetadadoAnexo) o;
        return referencia.equals(that.referencia)
            && tipoMime.equals(that.tipoMime)
            && atributos.equals(that.atributos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referencia, tipoMime, atributos);
    }
}
