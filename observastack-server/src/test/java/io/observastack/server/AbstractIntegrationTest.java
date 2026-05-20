package io.observastack.server;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe base para Integration Tests do observastack-server.
 *
 * <p>Inicializa um container PostgreSQL 16 via Testcontainers (compartilhado entre testes)
 * e configura o datasource dinamicamente via {@link DynamicPropertySource}.</p>
 *
 * <p>O Flyway executa automaticamente as migrations V1 e V2 no container de teste.</p>
 *
 * <p><strong>Pré-requisito:</strong> Docker deve estar disponível e em execução.
 * Em ambientes sem Docker, os ITs são pulados automaticamente pelo Testcontainers.</p>
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * Container PostgreSQL 16 compartilhado entre todos os ITs.
     * A anotação {@code @Container} com campo estático garante que o container
     * é iniciado uma vez para toda a suite de testes.
     */
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("observastack_test")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);  // Reusa o container entre execuções para acelerar builds

    /**
     * Injeta a URL do container Testcontainers no contexto Spring Boot.
     * Sobrescreve as configurações do application.yml de teste.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // p6spy wraps the JDBC URL do Testcontainers
        String testcontainersUrl = POSTGRES.getJdbcUrl()
            .replace("jdbc:postgresql://", "jdbc:p6spy:postgresql://");
        
        registry.add("spring.datasource.url", () -> testcontainersUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name",
            () -> "com.p6spy.engine.spy.P6SpyDriver");
    }
}
