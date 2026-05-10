package com.banco.reclamacoes.infrastructure.application;

import com.banco.reclamacoes.application.command.AnexoSolicitacaoCommand;
import com.banco.reclamacoes.application.port.MapeadorAnexosSolicitacao;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MapeadorAnexosSolicitacaoPadrao implements MapeadorAnexosSolicitacao {

    @Override
    public List<MetadadoAnexo> paraDominio(final List<AnexoSolicitacaoCommand> anexos) {
        if (anexos == null) {
            return List.of();
        }
        final var dominio = new ArrayList<MetadadoAnexo>();
        for (final var anexo : anexos) {
            final var atributos = anexo.atributos() == null ? Map.<String, String>of() : anexo.atributos();
            dominio.add(new MetadadoAnexo(anexo.referencia(), anexo.tipoMime(), atributos));
        }
        return dominio;
    }
}
