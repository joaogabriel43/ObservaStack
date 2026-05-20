package io.observastack.sdk.web;

import io.observastack.sdk.dispatch.SpanDispatcher;
import io.observastack.sdk.model.Span;
import io.observastack.sdk.model.SpanId;
import io.observastack.sdk.model.SpanStatus;
import io.observastack.sdk.model.TraceId;
import io.observastack.sdk.propagation.TraceContext;
import io.observastack.sdk.sampling.Sampler;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/**
 * Filter Jakarta EE para interceptação de requisições HTTP (Inbound).
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Extrair o cabeçalho W3C {@code traceparent} da requisição.</li>
 *   <li>Iniciar um novo contexto de trace ou continuar o existente.</li>
 *   <li>Criar o {@link Span} representando o processamento da requisição.</li>
 *   <li>Após o processamento, finalizar o span e enviá-lo ao {@link SpanDispatcher}.</li>
 *   <li>Garantir a limpeza do {@link ThreadLocal} via {@code TraceContext.clear()}.</li>
 * </ul>
 */
public class ObservaStackFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ObservaStackFilter.class);
    private static final String TRACEPARENT_HEADER = "traceparent";

    private final Sampler sampler;
    private final SpanDispatcher dispatcher;
    private final double samplingRate;

    public ObservaStackFilter(Sampler sampler, SpanDispatcher dispatcher, double samplingRate) {
        this.sampler = sampler;
        this.dispatcher = dispatcher;
        this.samplingRate = samplingRate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse res)) {
            chain.doFilter(request, response);
            return;
        }

        String traceparent = req.getHeader(TRACEPARENT_HEADER);
        Optional<TraceContext> parentContext = TraceContext.fromTraceparent(traceparent);

        TraceContext currentContext;
        SpanId parentSpanId = null;

        if (parentContext.isPresent()) {
            TraceContext pCtx = parentContext.get();
            parentSpanId = pCtx.spanId(); // O span atual se torna filho do span recebido
            // Iniciamos um novo contexto, pois cada operação na nossa JVM precisa de um novo SpanId ativo
            currentContext = TraceContext.startWithIds(pCtx.traceId(), SpanId.generate(), pCtx.isSampled());
        } else {
            // Nenhum traceparent recebido, inicia um novo trace e aplica o sampler
            TraceId newTraceId = TraceId.generate();
            boolean sampled = sampler.shouldSample(newTraceId, samplingRate);
            currentContext = TraceContext.startWithIds(newTraceId, SpanId.generate(), sampled);
        }

        Instant startedAt = Instant.now();
        String operationName = req.getMethod() + " " + req.getRequestURI();

        try {
            chain.doFilter(request, response);
            
            finishAndDispatchSpan(currentContext, parentSpanId, operationName, startedAt, res.getStatus(), null);
        } catch (Exception e) {
            finishAndDispatchSpan(currentContext, parentSpanId, operationName, startedAt, 500, e);
            throw e;
        } finally {
            TraceContext.clear();
        }
    }

    private void finishAndDispatchSpan(TraceContext context, SpanId parentSpanId, String operationName,
                                       Instant startedAt, int statusCode, Exception error) {
        if (!context.isSampled()) {
            return;
        }

        SpanStatus status = (statusCode >= 500 || error != null) ? SpanStatus.ERROR : SpanStatus.OK;

        Span.Builder builder = Span.builderWithSpanId(context.traceId(), context.spanId(), operationName)
                .startedAt(startedAt)
                .endedAt(Instant.now())
                .status(status)
                .attribute("http.status_code", String.valueOf(statusCode));

        if (parentSpanId != null) {
            builder.parentSpanId(parentSpanId);
        }
        
        if (error != null) {
            builder.attribute("error.message", error.getMessage());
        }

        Span span = builder.build();
        
        // Exibir no log conforme solicitado ("exibindo traceId, spanId, parentSpanId, duration_ms e operation_name")
        log.info("Span concluído: traceId={}, spanId={}, parentSpanId={}, duration_ms={}, operation_name={}",
                span.traceId(), span.spanId(), span.parentSpanId().map(SpanId::value).orElse("null"),
                span.durationMs().orElse(0L), span.operationName());

        dispatcher.enqueue(span);
    }
}
