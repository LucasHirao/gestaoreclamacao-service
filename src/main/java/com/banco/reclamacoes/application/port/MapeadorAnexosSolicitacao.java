package com.banco.reclamacoes.application.port;

import com.banco.reclamacoes.application.command.AnexoSolicitacaoCommand;
import com.banco.reclamacoes.domain.valueobject.MetadadoAnexo;
import java.util.List;

/** Traduz anexos do comando de aplicação para os value objects de domínio. */
public interface MapeadorAnexosSolicitacao {

    List<MetadadoAnexo> paraDominio(List<AnexoSolicitacaoCommand> anexos);
}
