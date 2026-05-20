package io.observastack.sdk.model;

/**
 * Enum representando o status de um Trace ou Span.
 * Alinhado com OpenTelemetry Status Codes para compatibilidade futura.
 */
public enum SpanStatus {

    /**
     * O span foi concluído com sucesso.
     */
    OK,

    /**
     * O span foi concluído com erro.
     */
    ERROR,

    /**
     * O status não foi definido explicitamente (estado padrão no início do span).
     */
    UNSET
}
