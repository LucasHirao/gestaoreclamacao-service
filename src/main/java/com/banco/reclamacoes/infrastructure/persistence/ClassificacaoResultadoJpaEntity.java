package com.banco.reclamacoes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classificacoes_resultado")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ClassificacaoResultadoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamacao_id", nullable = false, unique = true)
    ReclamacaoJpaEntity reclamacao;

    @Column(length = 8192)
    String payload;
}
