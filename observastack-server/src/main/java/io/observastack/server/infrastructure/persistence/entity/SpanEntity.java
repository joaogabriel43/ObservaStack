package io.observastack.server.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * JPA Entity para a tabela {@code spans}.
 *
 * <p>Pertence exclusivamente à camada {@code infrastructure}. A coluna
 * {@code attributes} usa {@code JSONB} do PostgreSQL via {@link SqlTypes#JSON}.</p>
 */
@Entity
@Table(
    name = "spans",
    indexes = {
        @Index(name = "idx_spans_trace_id", columnList = "trace_id"),
        @Index(name = "idx_spans_status", columnList = "status"),
        @Index(name = "idx_spans_duration", columnList = "duration_ms")
    }
)
public class SpanEntity {

    @Id
    @Column(name = "span_id", length = 32, nullable = false)
    private String spanId;

    @Column(name = "trace_id", length = 32, nullable = false)
    private String traceId;

    @Column(name = "parent_span_id", length = 32)
    private String parentSpanId;

    @Column(name = "operation_name", length = 512, nullable = false)
    private String operationName;

    @Column(name = "started_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant startedAt;

    @Column(name = "ended_at", columnDefinition = "TIMESTAMPTZ")
    private Instant endedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private SpanStatusJpa status;

    /**
     * Atributos arbitrários do span, armazenados como JSONB no PostgreSQL.
     * Mapeado via {@link SqlTypes#JSON} do Hibernate 6.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", nullable = false, columnDefinition = "JSONB")
    private Map<String, String> attributes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    protected SpanEntity() {}

    public SpanEntity(String spanId, String traceId, String parentSpanId,
                      String operationName, Instant startedAt, Instant endedAt,
                      Long durationMs, SpanStatusJpa status, Map<String, String> attributes) {
        this.spanId = spanId;
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.operationName = operationName;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationMs = durationMs;
        this.status = status;
        this.attributes = attributes;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getSpanId() { return spanId; }
    public String getTraceId() { return traceId; }
    public String getParentSpanId() { return parentSpanId; }
    public String getOperationName() { return operationName; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public Long getDurationMs() { return durationMs; }
    public SpanStatusJpa getStatus() { return status; }
    public Map<String, String> getAttributes() { return attributes; }
    public Instant getCreatedAt() { return createdAt; }
}
