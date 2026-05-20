package io.observastack.server.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * JPA Entity para a tabela {@code traces}.
 *
 * <p>Pertence exclusivamente à camada {@code infrastructure}.
 * O campo {@code duration_ms} é calculado pelo banco como coluna gerada.</p>
 */
@Entity
@Table(
    name = "traces",
    indexes = {
        @Index(name = "idx_traces_trace_id", columnList = "trace_id"),
        @Index(name = "idx_traces_status", columnList = "status"),
        @Index(name = "idx_traces_duration", columnList = "duration_ms")
    }
)
public class TraceEntity {

    @Id
    @Column(name = "trace_id", length = 32, nullable = false)
    private String traceId;

    @Column(name = "service_name", length = 255, nullable = false)
    private String serviceName;

    @Column(name = "started_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant startedAt;

    @Column(name = "ended_at", columnDefinition = "TIMESTAMPTZ")
    private Instant endedAt;

    /**
     * Coluna gerada pelo banco: {@code (ended_at - started_at) em milissegundos}.
     * Inserida/atualizada pelo PostgreSQL, leitura apenas pelo JPA.
     */
    @Column(name = "duration_ms", insertable = false, updatable = false)
    private Long durationMs;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private SpanStatusJpa status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    protected TraceEntity() {}

    public TraceEntity(String traceId, String serviceName, Instant startedAt,
                       Instant endedAt, SpanStatusJpa status) {
        this.traceId = traceId;
        this.serviceName = serviceName;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getTraceId() { return traceId; }
    public String getServiceName() { return serviceName; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public Long getDurationMs() { return durationMs; }
    public SpanStatusJpa getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
