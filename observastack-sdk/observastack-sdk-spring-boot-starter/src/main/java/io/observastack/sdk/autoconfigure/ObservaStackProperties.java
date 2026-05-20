package io.observastack.sdk.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração do ObservaStack SDK para Spring Boot.
 *
 * <p>Configurável via {@code application.yml} ou {@code application.properties}:</p>
 * <pre>{@code
 * observastack:
 *   enabled: true
 *   server-url: http://localhost:8080
 *   service-name: minha-aplicacao
 *   sampling-rate: 1.0
 * }</pre>
 */
@ConfigurationProperties(prefix = "observastack")
public class ObservaStackProperties {

    /** Habilita ou desabilita o SDK completamente. */
    private boolean enabled = true;

    /** URL base do ObservaStack Server para envio de traces. */
    private String serverUrl = "http://localhost:8080";

    /** Nome do serviço que será registrado nos traces. */
    private String serviceName = "unknown-service";

    /**
     * Taxa de amostragem head-based determinístico.
     * Valor entre 0.0 (nenhum trace) e 1.0 (todos os traces).
     */
    private double samplingRate = 1.0;

    /** Tamanho máximo da fila de spans em memória. */
    private int bufferMaxSize = 1000;

    // Getters e Setters

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public double getSamplingRate() { return samplingRate; }
    public void setSamplingRate(double samplingRate) { this.samplingRate = samplingRate; }

    public int getBufferMaxSize() { return bufferMaxSize; }
    public void setBufferMaxSize(int bufferMaxSize) { this.bufferMaxSize = bufferMaxSize; }
}
