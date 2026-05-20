package io.observastack.server.infrastructure.persistence.adapter;

import io.observastack.server.domain.model.Trace;
import io.observastack.server.domain.model.TraceStatus;
import io.observastack.server.domain.port.TraceRepository;
import io.observastack.server.infrastructure.persistence.entity.SpanStatusJpa;
import io.observastack.server.infrastructure.persistence.entity.TraceEntity;
import io.observastack.server.infrastructure.persistence.repository.TraceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa o port {@link TraceRepository} usando Spring Data JPA.
 *
 * <p>Responsável por converter entre o modelo de domínio ({@link Trace}) e
 * a entidade JPA ({@link TraceEntity}). Pertence à camada {@code infrastructure}.</p>
 */
@Component
public class TraceRepositoryAdapter implements TraceRepository {

    private final TraceJpaRepository jpaRepository;

    public TraceRepositoryAdapter(TraceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Trace save(Trace trace) {
        TraceEntity entity = toEntity(trace);
        TraceEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Trace> findByTraceId(String traceId) {
        return jpaRepository.findByTraceId(traceId).map(this::toDomain);
    }

    @Override
    public List<Trace> findByServiceName(String serviceName, int limit) {
        return jpaRepository.findByServiceNameOrderByStartedAtDesc(serviceName, limit)
            .stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public boolean existsByTraceId(String traceId) {
        return jpaRepository.existsByTraceId(traceId);
    }

    // -------------------------------------------------------------------------
    // Mappers (domain <-> entity)
    // -------------------------------------------------------------------------

    private TraceEntity toEntity(Trace trace) {
        return new TraceEntity(
            trace.traceId(),
            trace.serviceName(),
            trace.startedAt(),
            trace.endedAt(),
            toStatusJpa(trace.status())
        );
    }

    private Trace toDomain(TraceEntity entity) {
        return Trace.builder()
            .traceId(entity.getTraceId())
            .serviceName(entity.getServiceName())
            .startedAt(entity.getStartedAt())
            .endedAt(entity.getEndedAt())
            .status(toDomainStatus(entity.getStatus()))
            .build();
    }

    private SpanStatusJpa toStatusJpa(TraceStatus status) {
        return switch (status) {
            case OK -> SpanStatusJpa.OK;
            case ERROR -> SpanStatusJpa.ERROR;
            case UNSET -> SpanStatusJpa.UNSET;
        };
    }

    private TraceStatus toDomainStatus(SpanStatusJpa status) {
        if (status == null) return TraceStatus.UNSET;
        return switch (status) {
            case OK -> TraceStatus.OK;
            case ERROR -> TraceStatus.ERROR;
            case UNSET -> TraceStatus.UNSET;
        };
    }
}
