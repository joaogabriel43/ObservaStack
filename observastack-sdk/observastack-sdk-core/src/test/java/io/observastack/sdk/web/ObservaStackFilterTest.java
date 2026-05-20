package io.observastack.sdk.web;

import io.observastack.sdk.dispatch.SpanDispatcher;
import io.observastack.sdk.propagation.TraceContext;
import io.observastack.sdk.sampling.Sampler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ObservaStackFilterTest {

    @RepeatedTest(50) // Garante que a limpeza de ThreadLocal é safe
    void shouldCleanThreadLocalAfterFilterExecution() throws Exception {
        Sampler sampler = mock(Sampler.class);
        when(sampler.shouldSample(any(), anyDouble())).thenReturn(true);

        SpanDispatcher dispatcher = mock(SpanDispatcher.class);
        ObservaStackFilter filter = new ObservaStackFilter(sampler, dispatcher, 1.0);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("traceparent")).thenReturn(null);
        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURI()).thenReturn("/test");

        // Simulamos o filtro executando
        filter.doFilter(req, res, chain);

        // O TraceContext foi removido
        Optional<TraceContext> ctx = TraceContext.current();
        assertThat(ctx).isEmpty();

        // O dispatcher foi notificado
        verify(dispatcher).enqueue(any());
    }
}
