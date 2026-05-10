package com.banco.reclamacoes.application.usecase;

import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.application.query.BuscarReclamacaoPorProtocoloQuery;
import com.banco.reclamacoes.application.query.BuscarReclamacoesFiltrosQuery;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsultarReclamacaoUseCase {

    private final ReclamacaoRepositoryPort repositorio;

    public Optional<Reclamacao> porProtocolo(final BuscarReclamacaoPorProtocoloQuery query) {
        return repositorio.buscarPorProtocolo(Protocolo.de(query.protocolo()));
    }

    public List<Reclamacao> listar(final BuscarReclamacoesFiltrosQuery query) {
        return repositorio.buscar(
                clienteIdOpcional(query),
                query.status(),
                query.categoria(),
                query.pagina(),
                query.tamanho());
    }

    private static ClienteId clienteIdOpcional(final BuscarReclamacoesFiltrosQuery query) {
        return query.clienteId() == null ? null : ClienteId.de(query.clienteId());
    }
}
