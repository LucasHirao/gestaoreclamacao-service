CREATE TABLE reclamacoes (
    id UUID PRIMARY KEY,
    solicitacao_id VARCHAR(128) NOT NULL,
    protocolo VARCHAR(160) NOT NULL,
    cliente_id VARCHAR(128) NOT NULL,
    canal_origem VARCHAR(32) NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    categoria_principal VARCHAR(96),
    confianca_classificacao VARCHAR(16),
    data_recebimento TIMESTAMP WITH TIME ZONE NOT NULL,
    deadline_at TIMESTAMP WITH TIME ZONE NOT NULL,
    alert_at TIMESTAMP WITH TIME ZONE NOT NULL,
    alerta_emitido BOOLEAN NOT NULL DEFAULT FALSE,
    integracao_publicada BOOLEAN NOT NULL DEFAULT FALSE,
    correlation_id VARCHAR(160) NOT NULL,
    idempotency_key VARCHAR(256) NOT NULL,
    resultado_classificacao_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (solicitacao_id),
    UNIQUE (protocolo),
    UNIQUE (idempotency_key)
);

CREATE INDEX idx_reclamacoes_cliente ON reclamacoes (cliente_id);
CREATE INDEX idx_reclamacoes_status ON reclamacoes (status);
CREATE INDEX idx_reclamacoes_categoria ON reclamacoes (categoria_principal);
CREATE INDEX idx_reclamacoes_alertas ON reclamacoes (alert_at)
    WHERE alerta_emitido = FALSE AND status = 'ABERTA';

CREATE TABLE reclamacao_historico (
    id BIGSERIAL PRIMARY KEY,
    reclamacao_id UUID NOT NULL REFERENCES reclamacoes (id) ON DELETE CASCADE,
    tipo VARCHAR(128) NOT NULL,
    detalhe TEXT NOT NULL,
    ocorrido_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reclamacao_anexos_metadados (
    id BIGSERIAL PRIMARY KEY,
    reclamacao_id UUID NOT NULL REFERENCES reclamacoes (id) ON DELETE CASCADE,
    referencia VARCHAR(512) NOT NULL,
    tipo_mime VARCHAR(160) NOT NULL,
    atributos_json TEXT NOT NULL DEFAULT '{}'
);

CREATE TABLE classificacoes_resultado (
    id BIGSERIAL PRIMARY KEY,
    reclamacao_id UUID NOT NULL REFERENCES reclamacoes (id) ON DELETE CASCADE,
    payload TEXT NOT NULL
);

CREATE TABLE agenda_sla (
    id BIGSERIAL PRIMARY KEY,
    reclamacao_id UUID NOT NULL REFERENCES reclamacoes (id) ON DELETE CASCADE,
    alert_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deadline_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processado BOOLEAN NOT NULL DEFAULT FALSE
);
