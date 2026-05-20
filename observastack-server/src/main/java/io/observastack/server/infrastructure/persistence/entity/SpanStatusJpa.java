package io.observastack.server.infrastructure.persistence.entity;

/**
 * Enum JPA para o status de spans/traces na camada de infraestrutura.
 * Mapeado para a coluna VARCHAR(20) no PostgreSQL.
 */
public enum SpanStatusJpa {
    OK,
    ERROR,
    UNSET
}
