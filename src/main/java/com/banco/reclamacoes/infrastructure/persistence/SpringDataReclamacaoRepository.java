package com.banco.reclamacoes.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataReclamacaoRepository
    extends JpaRepository<ReclamacaoJpaEntity, UUID>, JpaSpecificationExecutor<ReclamacaoJpaEntity> {

    Optional<ReclamacaoJpaEntity> findBySolicitacaoId(final String solicitacaoId);

    Optional<ReclamacaoJpaEntity> findByIdempotencyKey(final String idempotencyKey);

    Optional<ReclamacaoJpaEntity> findByProtocolo(final String protocolo);

    @Query("""
            select r from ReclamacaoJpaEntity r
            where r.alertAt <= :momento
              and r.alertaEmitido = false
              and r.status = 'ABERTA'
            order by r.alertAt asc""")
    List<ReclamacaoJpaEntity> alertas(
        @Param("momento") final Instant momento, final Pageable pageable);

    @Query("""
            select r from ReclamacaoJpaEntity r
            where r.deadlineAt <= :limiteDeadline
              and r.status = 'ABERTA'
            order by r.deadlineAt asc""")
    List<ReclamacaoJpaEntity> proximasAoVencimento(
        @Param("limiteDeadline") final Instant limite, final Pageable pageable);
}
