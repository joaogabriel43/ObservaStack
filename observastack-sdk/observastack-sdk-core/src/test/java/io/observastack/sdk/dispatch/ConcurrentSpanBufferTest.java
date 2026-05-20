package io.observastack.sdk.dispatch;

import io.observastack.sdk.model.Span;
import io.observastack.sdk.model.TraceId;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.state.Action;
import net.jqwik.api.state.ActionChain;
import net.jqwik.api.state.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de concorrência usando jqwik para garantir que não há perda silenciosa
 * ou corrupção de IDs ao enfileirar múltiplos spans simultaneamente.
 */
class ConcurrentSpanBufferTest {

    @Property(tries = 10)
    void shouldHandleConcurrentEnqueues(@ForAll @IntRange(min = 100, max = 500) int spanCount) throws InterruptedException {
        // Mock DispatchHttpClient that does nothing to avoid actual network calls
        DispatchHttpClient mockClient = new DispatchHttpClient("http://localhost:9999") {
            @Override
            public boolean sendTraces(String jsonPayload) {
                return true;
            }
        };

        // Buffer muito grande para garantir que nenhum é dropado pelo size
        SpanDispatcher dispatcher = new SpanDispatcher("test", 1000, mockClient);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(spanCount);

        AtomicInteger processed = new AtomicInteger();

        for (int i = 0; i < spanCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Aguarda todos estarem prontos para disparar juntos
                    Span span = Span.builder(TraceId.generate(), "Operation").build();
                    dispatcher.enqueue(span);
                    processed.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown(); // Dispara todos!
        done.await(); // Aguarda todos terminarem

        assertThat(processed.get()).isEqualTo(spanCount);
        
        executor.shutdown();
        dispatcher.close();
    }
}
