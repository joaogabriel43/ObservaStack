package io.observastack.server.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Entidade de domínio representando um Span — operação individual dentro de um Trace.
 *
 * <p>Pertence à camada {@code domain}. Os atributos são armazenados como
 * {@code Map<String, String>} no domínio e persistidos como {@code JSONB} no PostgreSQL.</p>
 */
public final class Span {

    private final String spanId;
    private final String traceId;
    private final String parentSpanId;
    private final String operationName;
    private final Instant startedAt;
    private final Instant endedAt;
    private final Long durationMs;
    private final TraceStatus status;
    private final Map<String, String> attributes;

    private Span(Builder builder) {
        this.spanId = builder.spanId;
        this.traceId = builder.traceId;
        this.parentSpanId = builder.parentSpanId;
        this.operationName = builder.operationName;
        this.startedAt = builder.startedAt;
        this.endedAt = builder.endedAt;
        this.durationMs = builder.durationMs;
        this.status = builder.status;
        this.attributes = builder.attributes == null ? Map.of() : Map.copyOf(builder.attributes);
    }

    public static Builder builder() { return new Builder(); }

    public String spanId() { return spanId; }
    public String traceId() { return traceId; }
    public String parentSpanId() { return parentSpanId; }
    public String operationName() { return operationName; }
    public Instant startedAt() { return startedAt; }
    public Instant endedAt() { return endedAt; }
    public Long durationMs() { return durationMs; }
    public TraceStatus status() { return status; }
    public Map<String, String> attributes() { return attributes; }

    public static final class Builder {
        private String spanId;
        private String traceId;
        private String parentSpanId;
        private String operationName;
        private Instant startedAt;
        private Instant endedAt;
        private Long durationMs;
        private TraceStatus status = TraceStatus.UNSET;
        private Map<String, String> attributes;

        public Builder spanId(String spanId) { this.spanId = spanId; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }
        public Builder parentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; return this; }
        public Builder operationName(String operationName) { this.operationName = operationName; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder endedAt(Instant endedAt) { this.endedAt = endedAt; return this; }
        public Builder durationMs(Long durationMs) { this.durationMs = durationMs; return this; }
        public Builder status(TraceStatus status) { this.status = status; return this; }
        public Builder attributes(Map<String, String> attributes) { this.attributes = attributes; return this; }

        public Span build() {
            Objects.requireNonNull(spanId, "spanId is required");
            Objects.requireNonNull(traceId, "traceId is required");
            Objects.requireNonNull(operationName, "operationName is required");
            return new Span(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Span span)) return false;
        return Objects.equals(spanId, span.spanId);
    }

    @Override
    public int hashCode() { return Objects.hash(spanId); }

    @Override
    public String toString() {
        return "Span{spanId='" + spanId + "', traceId='" + traceId +
            "', operationName='" + operationName + "', status=" + status + "}";
    }
}
