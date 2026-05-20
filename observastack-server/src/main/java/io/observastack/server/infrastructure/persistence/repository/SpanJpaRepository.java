package io.observastack.server.infrastructure.persistence.repository;

import io.observastack.server.infrastructure.persistence.entity.SpanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository para a entidade {@link SpanEntity}.
 * Pertence à camada {@code infrastructure}.
 */
public interface SpanJpaRepository extends JpaRepository<SpanEntity, String> {

    Optional<SpanEntity> findBySpanId(String spanId);

    List<SpanEntity> findByTraceIdOrderByStartedAtAsc(String traceId);
}
