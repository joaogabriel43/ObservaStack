package io.observastack.sdk.model;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários e property-based para {@link TraceId} e {@link SpanId}.
 */
class IdModelsTest {

    // --- TraceId ---

    @Test
    void traceId_shouldGenerateValid32HexString() {
        TraceId id = TraceId.generate();
        assertNotNull(id.value());
        assertEquals(32, id.value().length());
        assertTrue(id.value().matches("[0-9a-f]{32}"));
    }

    @Test
    void traceId_shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> TraceId.of(null));
    }

    @Test
    void traceId_shouldRejectInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> TraceId.of("tooshort"));
        assertThrows(IllegalArgumentException.class, () -> TraceId.of("a".repeat(33)));
    }

    @Test
    void traceId_shouldRejectUppercaseHex() {
        assertThrows(IllegalArgumentException.class,
            () -> TraceId.of("AAAABBBBCCCCDDDD1111222233334444"));
    }

    // --- SpanId ---

    @Test
    void spanId_shouldGenerateValid16HexString() {
        SpanId id = SpanId.generate();
        assertNotNull(id.value());
        assertEquals(16, id.value().length());
        assertTrue(id.value().matches("[0-9a-f]{16}"));
    }

    @Test
    void spanId_shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> SpanId.of(null));
    }

    // --- Property-based ---

    @Property
    @Label("TraceIds gerados devem ser únicos (probabilidade)")
    void traceIds_shouldBeUnique(@ForAll @net.jqwik.api.constraints.IntRange(min = 2, max = 100) int count) {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < count; i++) {
            ids.add(TraceId.generate().value());
        }
        assertEquals(count, ids.size(), "TraceIds devem ser únicos");
    }

    @Property
    @Label("SpanIds gerados devem ser únicos (probabilidade)")
    void spanIds_shouldBeUnique(@ForAll @net.jqwik.api.constraints.IntRange(min = 2, max = 100) int count) {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < count; i++) {
            ids.add(SpanId.generate().value());
        }
        assertEquals(count, ids.size(), "SpanIds devem ser únicos");
    }
}
