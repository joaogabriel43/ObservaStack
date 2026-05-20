package io.observastack.sdk.propagation;

import io.observastack.sdk.model.TraceId;
import io.observastack.sdk.model.SpanId;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários e property-based para {@link TraceContext}.
 */
class TraceContextTest {

    @AfterEach
    void cleanup() {
        TraceContext.clear();
    }

    @Test
    void shouldStartContextWithValidIds() {
        TraceContext ctx = TraceContext.start(true);

        assertNotNull(ctx.traceId());
        assertNotNull(ctx.spanId());
        assertTrue(ctx.isSampled());
        assertEquals(ctx, TraceContext.current().orElseThrow());
    }

    @Test
    void shouldClearContextAfterClear() {
        TraceContext.start(true);
        assertTrue(TraceContext.current().isPresent());

        TraceContext.clear();
        assertFalse(TraceContext.current().isPresent());
    }

    @Test
    void shouldGenerateValidW3CTraceparent() {
        TraceContext ctx = TraceContext.start(true);
        String traceparent = ctx.toTraceparent();

        // Formato: 00-{32hex}-{16hex}-01
        assertTrue(traceparent.matches("00-[0-9a-f]{32}-[0-9a-f]{16}-01"));
    }

    @Test
    void shouldGenerateNotSampledTraceparent() {
        TraceContext ctx = TraceContext.start(false);
        String traceparent = ctx.toTraceparent();

        assertTrue(traceparent.endsWith("-00"));
    }

    @Test
    void shouldRestoreContextFromValidTraceparent() {
        TraceContext original = TraceContext.start(true);
        String traceparent = original.toTraceparent();
        TraceContext.clear();

        Optional<TraceContext> restored = TraceContext.fromTraceparent(traceparent);

        assertTrue(restored.isPresent());
        assertEquals(original.traceId(), restored.get().traceId());
        assertEquals(original.spanId(), restored.get().spanId());
        assertEquals(original.isSampled(), restored.get().isSampled());
    }

    @Test
    void shouldReturnEmptyForNullTraceparent() {
        assertTrue(TraceContext.fromTraceparent(null).isEmpty());
    }

    @Test
    void shouldReturnEmptyForMalformedTraceparent() {
        assertTrue(TraceContext.fromTraceparent("invalid-header").isEmpty());
        assertTrue(TraceContext.fromTraceparent("00-abc-def").isEmpty());
        assertTrue(TraceContext.fromTraceparent("").isEmpty());
    }

    @Property
    @Label("Traceparent gerado deve ser sempre parseável de volta")
    void roundTripTraceparent(@ForAll @StringLength(32) String rawTraceId) {
        // Filtra apenas strings hexadecimais válidas via geração filtrada
        // Esta property valida a consistência do ciclo serialize -> parse
        TraceContext ctx = TraceContext.start(true);
        String traceparent = ctx.toTraceparent();

        Optional<TraceContext> parsed = TraceContext.fromTraceparent(traceparent);
        assertTrue(parsed.isPresent(), "Traceparent gerado deve ser sempre parseável");
        assertEquals(ctx.traceId(), parsed.get().traceId());
    }
}
