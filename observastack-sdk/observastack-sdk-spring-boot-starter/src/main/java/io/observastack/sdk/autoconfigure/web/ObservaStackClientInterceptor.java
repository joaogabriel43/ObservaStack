package io.observastack.sdk.autoconfigure.web;

import io.observastack.sdk.propagation.TraceContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Interceptador para chamadas HTTP de saída (Outbound) utilizando RestTemplate ou RestClient.
 * Injeta o cabeçalho W3C traceparent na requisição caso haja um TraceContext ativo.
 */
public class ObservaStackClientInterceptor implements ClientHttpRequestInterceptor {

    private static final String TRACEPARENT_HEADER = "traceparent";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        
        Optional<TraceContext> context = TraceContext.current();
        if (context.isPresent()) {
            request.getHeaders().set(TRACEPARENT_HEADER, context.get().toTraceparent());
        }

        return execution.execute(request, body);
    }
}
