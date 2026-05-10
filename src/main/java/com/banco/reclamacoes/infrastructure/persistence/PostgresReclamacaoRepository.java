package com.banco.reclamacoes.infrastructure.persistence;

import com.banco.reclamacoes.application.port.output.ReclamacaoRepositoryPort;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.domain.valueobject.ClienteId;
import com.banco.reclamacoes.domain.valueobject.IdempotencyKey;
import com.banco.reclamacoes.domain.valueobject.Protocolo;
import com.banco.reclamacoes.domain.valueobject.ReclamacaoId;
import com.banco.reclamacoes.domain.valueobject.SolicitacaoId;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class PostgresReclamacaoRepository implements ReclamacaoRepositoryPort {

    private final SpringDataReclamacaoRepository springDataRepository;
    private final ReclamacaoMapper mapper;

    @Override
    public Reclamacao salvar(final Reclamacao reclamacao) {
        try {
            final var existente =
                springDataRepository.findById(reclamacao.id().valor());
            if (existente.isPresent()) {
                mapper.mesclarNaEntidadeExistente(reclamacao, existente.get());
                return mapper.paraDominio(springDataRepository.save(existente.get()));
            }
            final var novo = mapper.paraEntidade(reclamacao);
            return mapper.paraDominio(springDataRepository.save(novo));
        } catch (final Exception e) {
            throw new IllegalStateException("Falha ao persistir reclamação", e);
        }
    }

    @Override
    public Optional<Reclamacao> buscarPorSolicitacaoId(final SolicitacaoId solicitacaoId) {
        return springDataRepository
            .findBySolicitacaoId(solicitacaoId.valor())
            .map(this::mapearDominioSilencioso);
    }

    @Override
    public Optional<Reclamacao> buscarPorIdempotencyKey(final IdempotencyKey idempotencyKey) {
        return springDataRepository
            .findByIdempotencyKey(idempotencyKey.valor())
            .map(this::mapearDominioSilencioso);
    }

    @Override
    public Optional<Reclamacao> buscarPorProtocolo(final Protocolo protocolo) {
        return springDataRepository.findByProtocolo(protocolo.valor()).map(this::mapearDominioSilencioso);
    }

    @Override
    public Optional<Reclamacao> buscarPorId(final ReclamacaoId id) {
        return springDataRepository.findById(id.valor()).map(this::mapearDominioSilencioso);
    }

    @Override
    public List<Reclamacao> buscar(
        final ClienteId clienteId,
        final StatusReclamacao status,
        final CategoriaReclamacao categoria,
        final int pagina,
        final int tamanho
    ) {
        final Specification<ReclamacaoJpaEntity> spec = (root, query, cb) -> {
            final var predicados = new ArrayList<Predicate>();
            if (clienteId != null) {
                predicados.add(cb.equal(root.get("clienteId"), clienteId.valor()));
            }
            if (status != null) {
                predicados.add(cb.equal(root.get("status"), status.name()));
            }
            if (categoria != null) {
                predicados.add(cb.equal(root.get("categoriaPrincipal"), categoria.name()));
            }
            if (predicados.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
        final var paginaSpring = PageRequest.of(Math.max(pagina, 0), Math.max(tamanho, 1));
        return springDataRepository.findAll(spec, paginaSpring).stream()
            .map(this::mapearDominioSilencioso)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public List<Reclamacao> buscarReclamacoesComAlertaPendente(final Instant momento, final int limite) {
        return springDataRepository
            .alertas(momento, PageRequest.of(0, Math.max(limite, 1)))
            .stream()
            .map(this::mapearDominioSilencioso)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public List<Reclamacao> buscarProximasAoVencimento(final Instant ateDeadline, final int limite) {
        return springDataRepository
            .proximasAoVencimento(ateDeadline, PageRequest.of(0, Math.max(limite, 1)))
            .stream()
            .map(this::mapearDominioSilencioso)
            .filter(Objects::nonNull)
            .toList();
    }

    private Reclamacao mapearDominioSilencioso(final ReclamacaoJpaEntity entity) {
        try {
            return mapper.paraDominio(entity);
        } catch (final IOException e) {
            throw new IllegalStateException("Falha ao reidratar reclamação", e);
        }
    }
}
