-- V1: Cria a tabela 'traces'
-- ADR-2: Traces no PostgreSQL com tabelas normalizadas
-- Índices em: trace_id (PK), status, duration_ms

CREATE TABLE IF NOT EXISTS traces (
    trace_id     VARCHAR(32)  NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    started_at   TIMESTAMPTZ  NOT NULL,
    ended_at     TIMESTAMPTZ,

    -- Coluna gerada: calcula duration em milissegundos automaticamente
    duration_ms  BIGINT GENERATED ALWAYS AS (
        CASE
            WHEN ended_at IS NOT NULL
            THEN EXTRACT(EPOCH FROM (ended_at - started_at))::BIGINT * 1000
                 + (DATE_PART('milliseconds', ended_at - started_at))::BIGINT % 1000
            ELSE NULL
        END
    ) STORED,

    status       VARCHAR(20) NOT NULL DEFAULT 'UNSET'
                 CONSTRAINT traces_status_check CHECK (status IN ('OK', 'ERROR', 'UNSET')),

    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_traces PRIMARY KEY (trace_id)
);

-- Índice no status para queries de filtragem por saúde do sistema
CREATE INDEX IF NOT EXISTS idx_traces_status
    ON traces (status);

-- Índice na duration para queries de performance (traces lentos)
CREATE INDEX IF NOT EXISTS idx_traces_duration_ms
    ON traces (duration_ms)
    WHERE duration_ms IS NOT NULL;

-- Índice no service_name + started_at para queries de listagem por serviço
CREATE INDEX IF NOT EXISTS idx_traces_service_started
    ON traces (service_name, started_at DESC);

COMMENT ON TABLE traces IS 'Cabeçalho de traces distribuídos. Um trace agrupa todos os spans de uma requisição end-to-end.';
COMMENT ON COLUMN traces.trace_id IS 'Identificador único do trace — 32 chars hexadecimais lowercase (W3C TraceContext).';
COMMENT ON COLUMN traces.duration_ms IS 'Duração total do trace em milissegundos. Coluna gerada automaticamente pelo PostgreSQL.';
COMMENT ON COLUMN traces.status IS 'Status final do trace: OK, ERROR ou UNSET.';
