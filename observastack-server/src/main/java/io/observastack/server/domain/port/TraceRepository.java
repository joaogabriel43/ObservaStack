package io.observastack.server.domain.port;

import io.observastack.server.domain.model.Trace;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface de saída) do domínio para persistência de Traces.
 *
 * <p>Segue o padrão de Clean Architecture: o domínio define a interface
 * e a infraestrutura fornece a implementação. Nenhuma anotação JPA
 * ou Spring deve aparecer nesta interface.</p>
 *
 * <p>A implementação concreta está em {@code infrastructure.persistence}.</p>
 */
public interface TraceRepository {

    /**
     * Persiste um novo trace.
     *
     * @param trace o trace a ser salvo
     * @return o trace salvo (com quaisquer campos gerados pelo banco)
     */
    Trace save(Trace trace);

    /**
     * Busca um trace pelo seu ID.
     *
     * @param traceId identificador do trace
     * @return {@link Optional} contendo o trace, ou vazio se não encontrado
     */
    Optional<Trace> findByTraceId(String traceId);

    /**
     * Lista os traces mais recentes de um serviço.
     *
     * @param serviceName nome do serviço
     * @param limit       limite máximo de resultados
     * @return lista de traces ordenados por {@code startedAt} decrescente
     */
    List<Trace> findByServiceName(String serviceName, int limit);

    /**
     * Verifica se um trace com o ID fornecido existe.
     *
     * @param traceId identificador do trace
     * @return {@code true} se o trace existir
     */
    boolean existsByTraceId(String traceId);
}
