package io.observastack.sdk.sampling;

import io.observastack.sdk.model.TraceId;

/**
 * Implementação de sampling head-based determinístico (ADR-6).
 *
 * <p>A decisão de sampling é baseada nos primeiros 8 bytes do {@link TraceId},
 * convertidos para long e comparados com o threshold derivado da taxa de amostragem.
 * Dado o mesmo {@code traceId} e {@code samplingRate}, o resultado é sempre idêntico.</p>
 *
 * <p>Esta abordagem garante que traces completos sejam amostrados ou descartados
 * integralmente, sem traces parciais no banco de dados.</p>
 */
public final class DeterministicSampler implements Sampler {

    /**
     * Instância padrão que amostra 100% dos traces.
     * Equivale a chamar {@code shouldSample(traceId, 1.0)}.
     */
    public static final Sampler ALWAYS_ON = (traceId, rate) -> true;

    /**
     * Instância que não amostra nenhum trace.
     * Equivale a chamar {@code shouldSample(traceId, 0.0)}.
     */
    public static final Sampler ALWAYS_OFF = (traceId, rate) -> false;

    @Override
    public boolean shouldSample(TraceId traceId, double samplingRate) {
        if (samplingRate <= 0.0) return false;
        if (samplingRate >= 1.0) return true;

        // Usa os primeiros 16 chars (8 bytes) do traceId como base determinística
        String hexFragment = traceId.value().substring(0, 16);
        long traceIdLong = Long.parseUnsignedLong(hexFragment, 16);

        // Normaliza para [0.0, 1.0) usando apenas os 63 bits positivos
        double normalized = (traceIdLong & 0x7FFFFFFFFFFFFFFFL) / (double) Long.MAX_VALUE;

        return normalized < samplingRate;
    }
}
