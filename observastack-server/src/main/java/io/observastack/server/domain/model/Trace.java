package io.observastack.server.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidade de domínio representando um Trace distribuído.
 *
 * <p>Um Trace agrupa todos os Spans de uma requisição end-to-end.
 * Esta entidade pertence à camada {@code domain} e não conhece
 * detalhes de persistência (JPA) nem de apresentação (HTTP).</p>
 *
 * <p>Imutável após construção. Use o {@link Builder} para criar instâncias.</p>
 */
public final class Trace {

    private final String traceId;
    private final String serviceName;
    private final Instant startedAt;
    private final Instant endedAt;
    private final TraceStatus status;

    private Trace(Builder builder) {
        this.traceId = builder.traceId;
        this.serviceName = builder.serviceName;
        this.startedAt = builder.startedAt;
        this.endedAt = builder.endedAt;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String traceId() { return traceId; }
    public String serviceName() { return serviceName; }
    public Instant startedAt() { return startedAt; }
    public Instant endedAt() { return endedAt; }
    public TraceStatus status() { return status; }

    public Long durationMs() {
        if (startedAt == null || endedAt == null) return null;
        return endedAt.toEpochMilli() - startedAt.toEpochMilli();
    }

    public static final class Builder {
        private String traceId;
        private String serviceName;
        private Instant startedAt;
        private Instant endedAt;
        private TraceStatus status = TraceStatus.OK;

        public Builder traceId(String traceId) {
            this.traceId = Objects.requireNonNull(traceId);
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = Objects.requireNonNull(serviceName);
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder endedAt(Instant endedAt) {
            this.endedAt = endedAt;
            return this;
        }

        public Builder status(TraceStatus status) {
            this.status = Objects.requireNonNull(status);
            return this;
        }

        public Trace build() {
            Objects.requireNonNull(traceId, "traceId is required");
            Objects.requireNonNull(serviceName, "serviceName is required");
            Objects.requireNonNull(startedAt, "startedAt is required");
            return new Trace(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trace trace)) return false;
        return Objects.equals(traceId, trace.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId);
    }

    @Override
    public String toString() {
        return "Trace{traceId='" + traceId + "', serviceName='" + serviceName +
            "', status=" + status + "}";
    }
}
