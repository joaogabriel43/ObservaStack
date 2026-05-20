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

    @Bean
    @ConditionalOnMissingBean
    public io.observastack.sdk.dispatch.DispatchHttpClient dispatchHttpClient(ObservaStackProperties properties) {
        return new io.observastack.sdk.dispatch.DispatchHttpClient(properties.getServerUrl());
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public io.observastack.sdk.dispatch.SpanDispatcher spanDispatcher(
            ObservaStackProperties properties,
            io.observastack.sdk.dispatch.DispatchHttpClient dispatchHttpClient) {
        return new io.observastack.sdk.dispatch.SpanDispatcher(
                properties.getServiceName(),
                properties.getBufferMaxSize(),
                dispatchHttpClient
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public io.observastack.sdk.web.ObservaStackFilter observaStackFilter(
            Sampler sampler,
            io.observastack.sdk.dispatch.SpanDispatcher spanDispatcher,
            ObservaStackProperties properties) {
        return new io.observastack.sdk.web.ObservaStackFilter(
                sampler,
                spanDispatcher,
                properties.getSamplingRate()
        );
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<io.observastack.sdk.web.ObservaStackFilter> observaStackFilterRegistration(
            io.observastack.sdk.web.ObservaStackFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<io.observastack.sdk.web.ObservaStackFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public io.observastack.sdk.autoconfigure.web.ObservaStackClientInterceptor observaStackClientInterceptor() {
        return new io.observastack.sdk.autoconfigure.web.ObservaStackClientInterceptor();
    }

    @Bean
    public org.springframework.boot.web.client.RestTemplateCustomizer observaStackRestTemplateCustomizer(
            io.observastack.sdk.autoconfigure.web.ObservaStackClientInterceptor interceptor) {
        return restTemplate -> restTemplate.getInterceptors().add(interceptor);
    }
}
