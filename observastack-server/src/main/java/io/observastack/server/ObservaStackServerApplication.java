package io.observastack.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do ObservaStack Server.
 *
 * <p>Servidor central de observabilidade responsável por receber traces/spans
 * via HTTP, persistir no PostgreSQL e expor API de consulta.</p>
 *
 * <p>Clean Architecture é aplicada exclusivamente neste módulo (ADR per CLAUDE.md).</p>
 */
@SpringBootApplication
public class ObservaStackServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservaStackServerApplication.class, args);
    }
}
