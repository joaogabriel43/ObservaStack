package io.observastack.sdk.autoconfigure;

import io.observastack.sdk.ObservaStackConfig;
import io.observastack.sdk.sampling.DeterministicSampler;
import io.observastack.sdk.sampling.Sampler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfiguração Spring Boot para o ObservaStack SDK.
 *
 * <p>Registra automaticamente os beans necessários quando a dependência
 * {@code observastack-sdk-spring-boot-starter} é incluída no classpath.</p>
 *
 * <p>Ativada somente quando {@code observastack.enabled=true} (padrão) e
 * permite sobrescrita de qualquer bean via {@code @ConditionalOnMissingBean}.</p>
 */
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "observastack",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(ObservaStackProperties.class)
public class ObservaStackAutoConfiguration {

    /**
     * Registra o {@link Sampler} padrão (DeterministicSampler) se nenhum
     * bean customizado for fornecido pela aplicação.
     */
    @Bean
    @ConditionalOnMissingBean(Sampler.class)
    public Sampler observaStackSampler() {
        return new DeterministicSampler();
    }

    /**
     * Registra a configuração principal do SDK.
     */
    @Bean
    @ConditionalOnMissingBean(ObservaStackConfig.class)
    public ObservaStackConfig observaStackConfig(
        ObservaStackProperties properties,
        Sampler sampler
    ) {
        return ObservaStackConfig.builder()
            .serverUrl(properties.getServerUrl())
            .serviceName(properties.getServiceName())
            .samplingRate(properties.getSamplingRate())
            .sampler(sampler)
            .enabled(properties.isEnabled())
            .build();
    }
}
