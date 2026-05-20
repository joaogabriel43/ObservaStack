package io.observastack.sdk.model;

import java.util.Objects;

/**
 * Representa o identificador único de um Span.
 * Formato: string hexadecimal de 16 caracteres (64 bits), compatível com W3C TraceContext.
 *
 * @see <a href="https://www.w3.org/TR/trace-context/">W3C Trace Context</a>
 */
public record SpanId(String value) {

    public SpanId {
        Objects.requireNonNull(value, "SpanId value must not be null");
        if (value.isBlank() || value.length() != 16) {
            throw new IllegalArgumentException(
                "SpanId must be a 16-character hex string, got: '" + value + "'"
            );
        }
        if (!value.matches("[0-9a-f]{16}")) {
            throw new IllegalArgumentException(
                "SpanId must contain only lowercase hex characters [0-9a-f], got: '" + value + "'"
            );
        }
    }

    /**
     * Gera um novo SpanId aleatório.
     */
    public static SpanId generate() {
        return new SpanId(generateHex(8));
    }

    /**
     * Cria um SpanId a partir de um valor existente.
     */
    public static SpanId of(String value) {
        return new SpanId(value);
    }

    private static String generateHex(int bytes) {
        byte[] randomBytes = new byte[bytes];
        SECURE_RANDOM.nextBytes(randomBytes);
        StringBuilder sb = new StringBuilder(bytes * 2);
        for (byte b : randomBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    @Override
    public String toString() {
        return value;
    }
}
