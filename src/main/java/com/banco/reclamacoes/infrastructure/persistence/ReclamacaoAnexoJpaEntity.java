package com.banco.reclamacoes.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reclamacao_anexos_metadados")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ReclamacaoAnexoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamacao_id", nullable = false)
    ReclamacaoJpaEntity reclamacao;

    String referencia;
    String tipoMime;
    String atributosJson;
}
