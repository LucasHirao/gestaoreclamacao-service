package com.banco.reclamacoes.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reclamacoes")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ReclamacaoJpaEntity {

    @Id
    UUID id;

    String solicitacaoId;
    String protocolo;
    String clienteId;
    String canalOrigem;
    String descricao;
    String status;
    String categoriaPrincipal;
    String confiancaClassificacao;
    Instant dataRecebimento;
    Instant deadlineAt;
    Instant alertAt;
    boolean alertaEmitido;
    boolean integracaoPublicada;
    String correlationId;
    String idempotencyKey;

    /** Resumo rápido; detalhes completos em {@link ClassificacaoResultadoJpaEntity}. */
    @Column(length = 8192)
    String resultadoClassificacaoJson;

    Instant createdAt;
    Instant updatedAt;

    @OneToMany(mappedBy = "reclamacao", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReclamacaoHistoricoJpaEntity> historicos = new ArrayList<>();

    @OneToMany(mappedBy = "reclamacao", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReclamacaoAnexoJpaEntity> anexos = new ArrayList<>();

    @OneToOne(mappedBy = "reclamacao", cascade = CascadeType.ALL, orphanRemoval = true)
    ClassificacaoResultadoJpaEntity classificacaoResultado;

    @OneToMany(mappedBy = "reclamacao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<AgendaSlaJpaEntity> agendaSla = new ArrayList<>();
}
