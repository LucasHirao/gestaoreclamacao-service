package com.banco.reclamacoes.application.port.input;

import com.banco.reclamacoes.application.command.ProcessarSolicitacaoPadronizadaCommand;
import java.util.Optional;
import java.util.UUID;

public interface RegistrarSolicitacaoPadronizadaPort {

    Optional<UUID> processar(final ProcessarSolicitacaoPadronizadaCommand command);
}
