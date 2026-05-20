package io.observastack.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ObservaStack Demo Application.
 *
 * <p>Aplicação Spring Boot mínima para demonstração do observastack-sdk.
 * Utiliza o {@code observastack-sdk-spring-boot-starter} para autoconfiguração do SDK.</p>
 *
 * <p>Endpoints disponíveis:</p>
 * <ul>
 *   <li>{@code GET  /actuator/health}  — Status de saúde da aplicação</li>
 *   <li>{@code GET  /api/products}     — Lista todos os produtos</li>
 *   <li>{@code GET  /api/products/{id}} — Busca produto por ID</li>
 *   <li>{@code POST /api/orders}       — Cria um novo pedido</li>
 * </ul>
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.web.client.RestTemplate restTemplate(org.springframework.boot.web.client.RestTemplateBuilder builder) {
        return builder.build();
    }
}
