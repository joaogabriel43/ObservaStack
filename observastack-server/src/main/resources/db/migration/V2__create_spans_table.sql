-- V2: Cria a tabela 'spans'
-- ADR-2: Spans com suporte a JSONB para atributos arbitrários
-- Índices em: span_id (PK), trace_id (FK), status, duration_ms, attributes (GIN)

CREATE TABLE IF NOT EXISTS spans (
    span_id          VARCHAR(32)  NOT NULL,
    trace_id         VARCHAR(32)  NOT NULL,
    parent_span_id   VARCHAR(32),
    operation_name   VARCHAR(512) NOT NULL,
    started_at       TIMESTAMPTZ  NOT NULL,
    ended_at         TIMESTAMPTZ,

    -- duration_ms calculado na aplicação (diferente de traces, não é coluna gerada
    -- pois spans podem ser criados incrementalmente antes de ter ended_at)
    duration_ms      BIGINT,

    status           VARCHAR(20) NOT NULL DEFAULT 'UNSET'
                     CONSTRAINT spans_status_check CHECK (status IN ('OK', 'ERROR', 'UNSET')),

    -- Atributos arbitrários: chave-valor JSON (ADR-2 — JSONB para flexibilidade)
    -- Permite queries como: attributes @> '{"http.method": "GET"}'
    attributes       JSONB NOT NULL DEFAULT '{}',

    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_spans PRIMARY KEY (span_id),

    CONSTRAINT fk_spans_trace_id
        FOREIGN KEY (trace_id)
        REFERENCES traces (trace_id)
        ON DELETE CASCADE
        DEFERRABLE INITIALLY DEFERRED
);

-- Índice no trace_id para JOINs e queries de spans por trace
CREATE INDEX IF NOT EXISTS idx_spans_trace_id
    ON spans (trace_id);

-- Índice no status para queries de filtragem
CREATE INDEX IF NOT EXISTS idx_spans_status
    ON spans (status);

-- Índice na duration para análise de spans lentos
CREATE INDEX IF NOT EXISTS idx_spans_duration_ms
    ON spans (duration_ms)
    WHERE duration_ms IS NOT NULL;

-- Índice GIN nos attributes JSONB para queries de busca por atributos
-- Exemplo: WHERE attributes @> '{"http.method": "GET", "http.status_code": "200"}'
CREATE INDEX IF NOT EXISTS idx_spans_attributes_gin
    ON spans USING GIN (attributes);

-- Índice no parent_span_id para reconstrução da árvore de spans
CREATE INDEX IF NOT EXISTS idx_spans_parent_span_id
    ON spans (parent_span_id)
    WHERE parent_span_id IS NOT NULL;

COMMENT ON TABLE spans IS 'Spans individuais dentro de um trace. Um span representa uma operação atômica.';
COMMENT ON COLUMN spans.span_id IS 'Identificador único do span — 16 chars hexadecimais lowercase (W3C TraceContext).';
COMMENT ON COLUMN spans.parent_span_id IS 'Span pai. NULL indica que é o root span do trace.';
COMMENT ON COLUMN spans.attributes IS 'Atributos arbitrários do span em formato JSONB. Ex: {"http.method": "GET", "db.statement": "SELECT ..."}';
COMMENT ON COLUMN spans.duration_ms IS 'Duração do span em milissegundos.';
