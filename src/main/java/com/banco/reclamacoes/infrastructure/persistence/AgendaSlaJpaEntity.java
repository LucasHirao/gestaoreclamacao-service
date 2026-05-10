package com.banco.reclamacoes.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agenda_sla")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AgendaSlaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamacao_id", nullable = false)
    ReclamacaoJpaEntity reclamacao;

    Instant alertAt;
    Instant deadlineAt;
    boolean processado;
}
