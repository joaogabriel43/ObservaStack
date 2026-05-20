package io.observastack.sdk.model;

import io.observastack.sdk.model.SpanId;
import io.observastack.sdk.model.SpanStatus;
import io.observastack.sdk.model.TraceId;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa um Span — uma operação individual dentro de um Trace distribuído.
 *
 * <p>Um span captura o início e fim de uma operação, seu status e atributos
 * adicionais como pares chave-valor arbitrários (equivalente ao JSONB no banco).</p>
 */
public final class Span {

    private final SpanId spanId;
    private final TraceId traceId;
    private final SpanId parentSpanId; // null para root spans
    private final String operationName;
    private final Instant startedAt;
    private final Instant endedAt;
    private final SpanStatus status;
    private final Map<String, String> attributes;

    private Span(Builder builder) {
        this.spanId = builder.spanId;
        this.traceId = builder.traceId;
        this.parentSpanId = builder.parentSpanId;
        this.operationName = builder.operationName;
        this.startedAt = builder.startedAt;
        this.endedAt = builder.endedAt;
        this.status = builder.status;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(builder.attributes));
    }

    public static Builder builder(TraceId traceId, String operationName) {
        return new Builder(traceId, SpanId.generate(), operationName);
    }

    public static Builder builderWithSpanId(TraceId traceId, SpanId spanId, String operationName) {
        return new Builder(traceId, spanId, operationName);
    }

    public SpanId spanId() { return spanId; }
    public TraceId traceId() { return traceId; }
    public Optional<SpanId> parentSpanId() { return Optional.ofNullable(parentSpanId); }
    public String operationName() { return operationName; }
    public Instant startedAt() { return startedAt; }
    public Optional<Instant> endedAt() { return Optional.ofNullable(endedAt); }
    public SpanStatus status() { return status; }
    public Map<String, String> attributes() { return attributes; }

    public Optional<Long> durationMs() {
        if (startedAt == null || endedAt == null) return Optional.empty();
        return Optional.of(endedAt.toEpochMilli() - startedAt.toEpochMilli());
    }

    public static final class Builder {
        private final SpanId spanId;
        private final TraceId traceId;
        private SpanId parentSpanId;
        private final String operationName;
        private Instant startedAt = Instant.now();
        private Instant endedAt;
        private SpanStatus status = SpanStatus.UNSET;
        private final Map<String, String> attributes = new HashMap<>();

        private Builder(TraceId traceId, SpanId spanId, String operationName) {
            this.traceId = Objects.requireNonNull(traceId, "traceId must not be null");
            this.spanId = Objects.requireNonNull(spanId, "spanId must not be null");
            this.operationName = Objects.requireNonNull(operationName, "operationName must not be null");
        }

        public Builder parentSpanId(SpanId parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = Objects.requireNonNull(startedAt);
            return this;
        }

        public Builder endedAt(Instant endedAt) {
            this.endedAt = endedAt;
            return this;
        }

        public Builder status(SpanStatus status) {
            this.status = Objects.requireNonNull(status);
            return this;
        }

        public Builder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder attributes(Map<String, String> attrs) {
            this.attributes.putAll(attrs);
            return this;
        }

        public Span build() {
            return new Span(this);
        }
    }

    @Override
    public String toString() {
        return "Span{spanId=" + spanId + ", traceId=" + traceId +
            ", operationName='" + operationName + "', status=" + status + "}";
    }
}
