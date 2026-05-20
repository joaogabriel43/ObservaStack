package io.observastack.sdk.sampling;

import io.observastack.sdk.model.TraceId;

/**
 * Interface de sampling para o SDK ObservaStack.
 *
 * <p>Define o contrato para decisões de sampling head-based determinístico (ADR-6).
 * A decisão de sample é tomada no início do trace, baseada no {@link TraceId},
 * garantindo que um trace seja amostrado ou descartado integralmente.</p>
 *
 * <p>Implementações desta interface devem ser determinísticas: dado o mesmo
 * {@code traceId} e {@code samplingRate}, o resultado deve ser sempre o mesmo.</p>
 */
public interface Sampler {

    /**
     * Determina se um trace com o ID fornecido deve ser amostrado.
     *
     * @param traceId      identificador do trace sendo avaliado
     * @param samplingRate taxa de amostragem entre 0.0 (nenhum) e 1.0 (todos)
     * @return {@code true} se o trace deve ser coletado e enviado ao servidor
     */
    boolean shouldSample(TraceId traceId, double samplingRate);
}
