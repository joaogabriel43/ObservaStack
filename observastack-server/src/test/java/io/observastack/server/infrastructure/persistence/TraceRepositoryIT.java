package io.observastack.server.infrastructure.persistence;

import io.observastack.server.AbstractIntegrationTest;
import io.observastack.server.domain.model.Trace;
import io.observastack.server.domain.model.TraceStatus;
import io.observastack.server.domain.port.TraceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test para o {@link TraceRepository} com PostgreSQL real via Testcontainers.
 *
 * <p>Valida que:</p>
 * <ul>
 *   <li>As migrations Flyway V1 e V2 executam corretamente</li>
 *   <li>Operações CRUD na tabela {@code traces} funcionam</li>
 *   <li>O adapter mapeia corretamente entre domínio e JPA</li>
 * </ul>
 */
@DisplayName("TraceRepository — Integration Tests (Testcontainers + Flyway)")
@Transactional
class TraceRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private TraceRepository traceRepository;

    @Test
    @DisplayName("deve salvar e recuperar um trace pelo traceId")
    void shouldSaveAndFindTraceByTraceId() {
        // Given
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Trace trace = Trace.builder()
            .traceId("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4")
            .serviceName("observastack-demo-app")
            .startedAt(now)
            .endedAt(now.plusMillis(150))
            .status(TraceStatus.OK)
            .build();

        // When
        traceRepository.save(trace);
        Optional<Trace> found = traceRepository.findByTraceId("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().traceId()).isEqualTo("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4");
        assertThat(found.get().serviceName()).isEqualTo("observastack-demo-app");
        assertThat(found.get().status()).isEqualTo(TraceStatus.OK);
    }

    @Test
    @DisplayName("deve retornar Optional.empty() para traceId inexistente")
    void shouldReturnEmptyForNonExistentTraceId() {
        Optional<Trace> found = traceRepository.findByTraceId("ffffffffffffffffffffffffffffffff");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("deve verificar existência de trace corretamente")
    void shouldCheckTraceExistence() {
        // Given
        Trace trace = Trace.builder()
            .traceId("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
            .serviceName("test-service")
            .startedAt(Instant.now())
            .status(TraceStatus.OK)
            .build();
        traceRepository.save(trace);

        // Then
        assertThat(traceRepository.existsByTraceId("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")).isTrue();
        assertThat(traceRepository.existsByTraceId("cccccccccccccccccccccccccccccccc")).isFalse();
    }

    @Test
    @DisplayName("deve listar traces por serviceName")
    void shouldFindTracesByServiceName() {
        // Given
        Instant base = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        for (int i = 0; i < 3; i++) {
            traceRepository.save(Trace.builder()
                .traceId(String.format("dddddddddddddddddddddddddddddd%02d", i))
                .serviceName("my-service")
                .startedAt(base.plusSeconds(i))
                .status(TraceStatus.OK)
                .build());
        }

        // When
        List<Trace> traces = traceRepository.findByServiceName("my-service", 10);

        // Then
        assertThat(traces).hasSize(3);
        assertThat(traces).allMatch(t -> "my-service".equals(t.serviceName()));
    }

    @Test
    @DisplayName("deve executar as migrations Flyway V1 e V2 sem erros (smoke test)")
    void flywayMigrations_shouldRunWithoutErrors() {
        // Se chegamos aqui, o contexto Spring Boot subiu com sucesso,
        // o que implica que as migrations V1 e V2 foram executadas pelo Flyway
        // e o schema foi validado pelo Hibernate (ddl-auto: validate).
        // Este teste documenta intencionalmente essa garantia.
        assertThat(traceRepository).isNotNull();
    }
}
