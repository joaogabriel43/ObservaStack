package io.observastack.sdk.dispatch;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.observastack.sdk.model.Span;
import io.observastack.sdk.model.SpanStatus;
import io.observastack.sdk.model.TraceId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;

@WireMockTest
@DisplayName("SpanDispatcher — Integration Tests with WireMock")
class SpanDispatcherTest {

    private DispatchHttpClient httpClient;
    private SpanDispatcher dispatcher;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        String baseUrl = "http://localhost:" + wmRuntimeInfo.getHttpPort();
        httpClient = new DispatchHttpClient(baseUrl);
        // Cria o dispatcher com flush a cada 1 segundo (pra teste rápido) e buffer pequeno
        dispatcher = new SpanDispatcher("test-service", 50, httpClient);
    }

    @AfterEach
    void tearDown() {
        dispatcher.close();
    }

    @Test
    @DisplayName("Deve enviar spans com sucesso para o servidor disponível")
    void shouldDispatchSuccessfully() {
        stubFor(post(urlEqualTo("/api/v1/traces"))
                .willReturn(aResponse().withStatus(201)));

        Span span = Span.builder(TraceId.generate(), "GET /test")
                .status(SpanStatus.OK)
                .build();
        dispatcher.enqueue(span);
        dispatcher.flush();

        verify(1, postRequestedFor(urlEqualTo("/api/v1/traces"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.spans[0].operationName", equalTo("GET /test"))));
    }

    @Test
    @DisplayName("Deve reter spans no buffer se o servidor estiver indisponível e reenviar após recuperação")
    void shouldRetainSpansWhenServerUnavailableAndRetry() {
        // Servidor inicialmente responde com 503
        stubFor(post(urlEqualTo("/api/v1/traces"))
                .inScenario("Retry")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("Recovered"));

        // Depois ele recupera e responde 201
        stubFor(post(urlEqualTo("/api/v1/traces"))
                .inScenario("Retry")
                .whenScenarioStateIs("Recovered")
                .willReturn(aResponse().withStatus(201)));

        dispatcher.enqueue(Span.builder(TraceId.generate(), "Failed first").build());

        // Força o envio; o Resilience4j vai tomar 503 na primeira, esperar 2s, e 201 na segunda
        dispatcher.flush();

        // O request deve chegar e resultar em 201 no cenário Recovered
        verify(postRequestedFor(urlEqualTo("/api/v1/traces"))
                .withRequestBody(matchingJsonPath("$.spans[0].operationName", equalTo("Failed first"))));
    }
}
