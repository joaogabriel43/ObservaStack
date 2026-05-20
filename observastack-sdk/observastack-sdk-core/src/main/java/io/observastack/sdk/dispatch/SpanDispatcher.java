package io.observastack.sdk.dispatch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.observastack.sdk.model.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gerencia o buffer circular de spans e o envio assíncrono para o servidor.
 */
public class SpanDispatcher implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SpanDispatcher.class);
    private static final int BATCH_SIZE = 100;
    private static final int FLUSH_INTERVAL_SECONDS = 30;

    private final String serviceName;
    private final ArrayBlockingQueue<Span> buffer;
    private final DispatchHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executor;
    private final AtomicBoolean isFlushing = new AtomicBoolean(false);

    public SpanDispatcher(String serviceName, int maxBufferSize, DispatchHttpClient httpClient) {
        this.serviceName = serviceName;
        this.buffer = new ArrayBlockingQueue<>(maxBufferSize);
        this.httpClient = httpClient;
        
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "observastack-dispatcher");
            t.setDaemon(true);
            return t;
        });

        // Agendamento periódico para garantir envio mesmo com baixo volume
        this.executor.scheduleWithFixedDelay(this::flush, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Enfileira um span para envio.
     * <p>Se o buffer estiver cheio, descarta o span mais antigo (circular) e registra um aviso.</p>
     *
     * @param span o span finalizado
     */
    public void enqueue(Span span) {
        if (!buffer.offer(span)) {
            // Buffer cheio: descarta o mais antigo e tenta inserir novamente
            Span dropped = buffer.poll();
            if (dropped != null) {
                log.warn("Buffer cheio. Descartando span antigo para liberar espaço: traceId={}, spanId={}", 
                        dropped.traceId(), dropped.spanId());
            }
            // Tenta inserir novamente; se falhar, apenas ignoramos
            buffer.offer(span);
        }

        // Verifica se alcançou o batch size para flush imediato
        if (buffer.size() >= BATCH_SIZE) {
            executor.submit(this::flush);
        }
    }

    /**
     * Drena o buffer e envia os spans ao servidor.
     * Pode ser chamado manualmente para forçar o envio.
     */
    public void flush() {
        if (!isFlushing.compareAndSet(false, true)) {
            return; // Já está em andamento
        }

        try {
            int toDrain = Math.min(buffer.size(), BATCH_SIZE);
            if (toDrain == 0) {
                return;
            }

            List<Span> batch = new ArrayList<>(toDrain);
            buffer.drainTo(batch, toDrain);

            if (batch.isEmpty()) {
                return;
            }

            String jsonPayload = serializeBatch(batch);
            boolean success = httpClient.sendTraces(jsonPayload);

            if (!success) {
                // Servidor indisponível após retries. Devolvemos os spans pro buffer.
                // Devido à natureza circular, se o buffer encher durante o re-enqueue, 
                // eles serão perdidos. Isto é intencional para priorizar novos dados e evitar OOM.
                log.warn("Falha ao enviar {} spans. Retornando ao buffer para próxima tentativa.", batch.size());
                for (Span s : batch) {
                    enqueue(s); // Usa a própria lógica circular
                }
            } else {
                log.debug("Enviados {} spans com sucesso.", batch.size());
            }

        } catch (Exception e) {
            log.error("Erro inesperado no dispatcher", e);
        } finally {
            isFlushing.set(false);
            
            // Se ainda tem mais que um BATCH_SIZE, agenda novo flush imediato
            if (buffer.size() >= BATCH_SIZE) {
                executor.submit(this::flush);
            }
        }
    }

    private String serializeBatch(List<Span> spans) throws Exception {
        Payload payload = new Payload(serviceName, spans.stream().map(SpanDto::from).toList());
        return objectMapper.writeValueAsString(payload);
    }

    @Override
    public void close() {
        executor.shutdown();
        flush(); // Última tentativa de envio ao desligar
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -------------------------------------------------------------------------
    // DTOs para o contrato JSON (POST /api/v1/traces)
    // -------------------------------------------------------------------------

    private record Payload(String serviceName, List<SpanDto> spans) {}

    private record SpanDto(
            String traceId,
            String spanId,
            String parentSpanId,
            String operationName,
            Instant startedAt,
            Instant endedAt,
            String status,
            Map<String, String> attributes
    ) {
        static SpanDto from(Span span) {
            return new SpanDto(
                    span.traceId().value(),
                    span.spanId().value(),
                    span.parentSpanId().map(id -> id.value()).orElse(null),
                    span.operationName(),
                    span.startedAt(),
                    span.endedAt().orElse(null),
                    span.status().name(),
                    span.attributes()
            );
        }
    }
}
