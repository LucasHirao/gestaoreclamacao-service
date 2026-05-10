package com.banco.reclamacoes.application.port.output;

import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReclamacaoRepositoryPort {

    Reclamacao salvar(final Reclamacao reclamacao);

    Optional<Reclamacao> buscarPorSolicitacaoId(final SolicitacaoId solicitacaoId);

    Optional<Reclamacao> buscarPorIdempotencyKey(final IdempotencyKey idempotencyKey);

    Optional<Reclamacao> buscarPorProtocolo(final Protocolo protocolo);

    Optional<Reclamacao> buscarPorId(final ReclamacaoId id);

    List<Reclamacao> buscar(
        final ClienteId clienteId,
        final StatusReclamacao status,
        final CategoriaReclamacao categoria,
        final int pagina,
        final int tamanho);

    List<Reclamacao> buscarReclamacoesComAlertaPendente(final Instant momento, final int limite);

    List<Reclamacao> buscarProximasAoVencimento(final Instant ateDeadline, final int limite);
}
