package io.observastack.sdk;

import io.observastack.sdk.sampling.DeterministicSampler;
import io.observastack.sdk.sampling.Sampler;

import java.util.Objects;

/**
 * Configuração principal do ObservaStack SDK.
 *
 * <p>Ponto central de configuração para o SDK. Utiliza o padrão Builder
 * para construção fluente e imutabilidade após construção.</p>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * ObservaStackConfig config = ObservaStackConfig.builder()
 *     .serverUrl("http://localhost:8080")
 *     .serviceName("minha-aplicacao")
 *     .samplingRate(0.1) // amostra 10% dos traces
 *     .build();
 * }</pre>
 */
public final class ObservaStackConfig {

    private final String serverUrl;
    private final String serviceName;
    private final double samplingRate;
    private final Sampler sampler;
    private final boolean enabled;

    private ObservaStackConfig(Builder builder) {
        this.serverUrl = builder.serverUrl;
        this.serviceName = builder.serviceName;
        this.samplingRate = builder.samplingRate;
        this.sampler = builder.sampler;
        this.enabled = builder.enabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String serverUrl() { return serverUrl; }
    public String serviceName() { return serviceName; }
    public double samplingRate() { return samplingRate; }
    public Sampler sampler() { return sampler; }
    public boolean isEnabled() { return enabled; }

    public static final class Builder {
        private String serverUrl = "http://localhost:8080";
        private String serviceName = "unknown-service";
        private double samplingRate = 1.0;
        private Sampler sampler = new DeterministicSampler();
        private boolean enabled = true;

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = Objects.requireNonNull(serverUrl, "serverUrl must not be null");
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
            return this;
        }

        public Builder samplingRate(double samplingRate) {
            if (samplingRate < 0.0 || samplingRate > 1.0) {
                throw new IllegalArgumentException("samplingRate must be between 0.0 and 1.0");
            }
            this.samplingRate = samplingRate;
            return this;
        }

        public Builder sampler(Sampler sampler) {
            this.sampler = Objects.requireNonNull(sampler, "sampler must not be null");
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ObservaStackConfig build() {
            Objects.requireNonNull(serverUrl, "serverUrl is required");
            Objects.requireNonNull(serviceName, "serviceName is required");
            return new ObservaStackConfig(this);
        }
    }

    @Override
    public String toString() {
        return "ObservaStackConfig{" +
            "serverUrl='" + serverUrl + '\'' +
            ", serviceName='" + serviceName + '\'' +
            ", samplingRate=" + samplingRate +
            ", enabled=" + enabled +
            '}';
    }
}
