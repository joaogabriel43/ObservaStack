package io.observastack.sdk.dispatch;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP resiliente para envio de traces.
 *
 * <p>Utiliza {@link java.net.http.HttpClient} para minimizar dependências e
 * é envelopado com Resilience4j para tolerância a falhas (retries com backoff).</p>
 */
public class DispatchHttpClient {

    private static final Logger log = LoggerFactory.getLogger(DispatchHttpClient.class);

    private final HttpClient httpClient;
    private final Retry retry;
    private final URI endpointUri;

    public DispatchHttpClient(String serverUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        
        this.endpointUri = URI.create(serverUrl + "/api/v1/traces");

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2)) // Backoff simples
                .retryOnResult(response -> {
                    if (response instanceof HttpResponse<?> httpResp) {
                        return httpResp.statusCode() >= 500;
                    }
                    return false;
                })
                .retryExceptions(Exception.class)
                .build();

        this.retry = Retry.of("dispatch-http-client", retryConfig);
    }

    /**
     * Envia o payload JSON ao servidor.
     *
     * @param jsonPayload payload JSON contendo lista de spans
     * @return true se enviado com sucesso (HTTP 2xx), false caso contrário (após os retries)
     */
    public boolean sendTraces(String jsonPayload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(endpointUri)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = retry.executeSupplier(() -> {
                try {
                    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (Exception e) {
                    throw new RuntimeException("Falha ao enviar traces", e);
                }
            });

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                log.warn("Falha no envio de traces. HTTP status: {}", response.statusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Erro fatal no envio de traces para o servidor após retries", e);
            return false;
        }
    }
}
