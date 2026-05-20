package io.observastack.server.infrastructure.persistence.repository;

import io.observastack.server.infrastructure.persistence.entity.TraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository para a entidade {@link TraceEntity}.
 * Pertence à camada {@code infrastructure}.
 */
public interface TraceJpaRepository extends JpaRepository<TraceEntity, String> {

    Optional<TraceEntity> findByTraceId(String traceId);

    boolean existsByTraceId(String traceId);

    @Query("SELECT t FROM TraceEntity t WHERE t.serviceName = :serviceName " +
           "ORDER BY t.startedAt DESC LIMIT :limit")
    List<TraceEntity> findByServiceNameOrderByStartedAtDesc(
        @Param("serviceName") String serviceName,
        @Param("limit") int limit
    );
}
