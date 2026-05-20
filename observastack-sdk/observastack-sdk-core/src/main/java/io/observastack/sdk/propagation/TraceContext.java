package io.observastack.sdk.propagation;

import io.observastack.sdk.model.SpanId;
import io.observastack.sdk.model.TraceId;

import java.util.Optional;

/**
 * Contexto de trace armazenado em {@link ThreadLocal}, seguindo o padrão W3C TraceContext.
 *
 * <p>Mantém o {@link TraceId} e o {@link SpanId} do span atual da thread em execução.
 * Não possui dependências do Spring Framework — pode ser utilizado em qualquer aplicação Java.</p>
 *
 * <p>Formato W3C traceparent: {@code 00-{traceId}-{spanId}-{flags}}</p>
 *
 * @see <a href="https://www.w3.org/TR/trace-context/">W3C Trace Context Specification</a>
 * @see <a href="https://www.w3.org/TR/trace-context/#traceparent-header">traceparent Header</a>
 */
public final class TraceContext {

    private static final ThreadLocal<TraceContext> CONTEXT = new ThreadLocal<>();

    /** Versão fixa do header W3C traceparent. */
    private static final String W3C_VERSION = "00";

    /** Flags: 01 = sampled. */
    private static final String FLAGS_SAMPLED = "01";

    /** Flags: 00 = not sampled. */
    private static final String FLAGS_NOT_SAMPLED = "00";

    private final TraceId traceId;
    private final SpanId spanId;
    private final boolean sampled;

    private TraceContext(TraceId traceId, SpanId spanId, boolean sampled) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.sampled = sampled;
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    /**
     * Inicia um novo contexto de trace com IDs gerados aleatoriamente.
     *
     * @param sampled indica se este trace será amostrado/enviado ao servidor
     * @return novo {@link TraceContext} configurado como contexto da thread atual
     */
    public static TraceContext start(boolean sampled) {
        TraceContext ctx = new TraceContext(TraceId.generate(), SpanId.generate(), sampled);
        CONTEXT.set(ctx);
        return ctx;
    }

    /**
     * Restaura um contexto de trace a partir do header W3C {@code traceparent}.
     *
     * <p>Formato esperado: {@code 00-{traceId32hex}-{spanId16hex}-{flags}}</p>
     *
     * @param traceparent valor do header traceparent
     * @return {@link Optional} contendo o contexto restaurado, ou vazio se o header for inválido
     */
    public static Optional<TraceContext> fromTraceparent(String traceparent) {
        if (traceparent == null || traceparent.isBlank()) {
            return Optional.empty();
        }
        String[] parts = traceparent.split("-");
        if (parts.length != 4) {
            return Optional.empty();
        }
        try {
            String version = parts[0];
            String rawTraceId = parts[1];
            String rawSpanId = parts[2];
            String flags = parts[3];

            if (!W3C_VERSION.equals(version)) {
                return Optional.empty();
            }

            TraceId traceId = TraceId.of(rawTraceId);
            SpanId spanId = SpanId.of(rawSpanId);
            boolean sampled = FLAGS_SAMPLED.equals(flags);

            TraceContext ctx = new TraceContext(traceId, spanId, sampled);
            CONTEXT.set(ctx);
            return Optional.of(ctx);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // -------------------------------------------------------------------------
    // ThreadLocal Access
    // -------------------------------------------------------------------------

    /**
     * Retorna o contexto de trace da thread atual, se existir.
     */
    public static Optional<TraceContext> current() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * Remove o contexto de trace da thread atual.
     * Deve ser chamado ao final de cada request para evitar vazamento de contexto.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    // -------------------------------------------------------------------------
    // W3C Header Generation
    // -------------------------------------------------------------------------

    /**
     * Gera o valor do header W3C {@code traceparent} para propagação.
     *
     * @return string no formato {@code 00-{traceId}-{spanId}-{flags}}
     */
    public String toTraceparent() {
        String flags = sampled ? FLAGS_SAMPLED : FLAGS_NOT_SAMPLED;
        return W3C_VERSION + "-" + traceId.value() + "-" + spanId.value() + "-" + flags;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public TraceId traceId() {
        return traceId;
    }

    public SpanId spanId() {
        return spanId;
    }

    public boolean isSampled() {
        return sampled;
    }

    @Override
    public String toString() {
        return "TraceContext{traceId=" + traceId + ", spanId=" + spanId + ", sampled=" + sampled + "}";
    }
}
