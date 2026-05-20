package io.observastack.sdk.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Representa o identificador único de um Trace.
 * Formato: string hexadecimal de 32 caracteres (128 bits), compatível com W3C TraceContext.
 *
 * @see <a href="https://www.w3.org/TR/trace-context/">W3C Trace Context</a>
 */
public record TraceId(String value) {

    public TraceId {
        Objects.requireNonNull(value, "TraceId value must not be null");
        if (value.isBlank() || value.length() != 32) {
            throw new IllegalArgumentException(
                "TraceId must be a 32-character hex string, got: '" + value + "'"
            );
        }
        if (!value.matches("[0-9a-f]{32}")) {
            throw new IllegalArgumentException(
                "TraceId must contain only lowercase hex characters [0-9a-f], got: '" + value + "'"
            );
        }
    }

    /**
     * Gera um novo TraceId aleatório.
     */
    public static TraceId generate() {
        return new TraceId(generateHex(16));
    }

    /**
     * Cria um TraceId a partir de um valor existente.
     */
    public static TraceId of(String value) {
        return new TraceId(value);
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
