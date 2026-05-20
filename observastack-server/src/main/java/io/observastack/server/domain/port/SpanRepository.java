package io.observastack.server.domain.port;

import io.observastack.server.domain.model.Span;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface de saída) do domínio para persistência de Spans.
 *
 * <p>Segue o padrão de Clean Architecture: o domínio define a interface
 * e a infraestrutura fornece a implementação.</p>
 */
public interface SpanRepository {

    /**
     * Persiste um novo span.
     *
     * @param span o span a ser salvo
     * @return o span salvo
     */
    Span save(Span span);

    /**
     * Busca todos os spans pertencentes a um trace.
     *
     * @param traceId identificador do trace
     * @return lista de spans ordenados por {@code startedAt} crescente
     */
    List<Span> findByTraceId(String traceId);

    /**
     * Busca um span pelo seu ID.
     *
     * @param spanId identificador do span
     * @return {@link Optional} contendo o span, ou vazio se não encontrado
     */
    Optional<Span> findBySpanId(String spanId);
}
