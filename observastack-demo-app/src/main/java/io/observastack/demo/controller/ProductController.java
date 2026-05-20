package io.observastack.demo.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller de demonstração para endpoints de Produtos.
 *
 * <p>Simula um catálogo de produtos com dados estáticos para fins de demonstração
 * da instrumentação do ObservaStack SDK.</p>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /** Catálogo de produtos mock para demonstração */
    private static final List<Map<String, Object>> PRODUCTS = List.of(
        Map.of("id", 1, "name", "Spring Boot 3 Handbook", "price", new BigDecimal("49.90"), "category", "books"),
        Map.of("id", 2, "name", "Observability Engineering", "price", new BigDecimal("79.90"), "category", "books"),
        Map.of("id", 3, "name", "Java 21 Performance Guide", "price", new BigDecimal("59.90"), "category", "books"),
        Map.of("id", 4, "name", "PostgreSQL Tuning Pro", "price", new BigDecimal("39.90"), "category", "books"),
        Map.of("id", 5, "name", "Distributed Systems Design", "price", new BigDecimal("89.90"), "category", "books")
    );

    /**
     * Lista todos os produtos disponíveis.
     *
     * @return HTTP 200 com lista de produtos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts() {
        return ResponseEntity.ok(Map.of(
            "data", PRODUCTS,
            "total", PRODUCTS.size(),
            "page", 1
        ));
    }

    /**
     * Busca um produto pelo ID.
     *
     * @param id identificador do produto (deve ser positivo)
     * @return HTTP 200 com o produto, ou HTTP 404 se não encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(
        @PathVariable @Positive(message = "ID must be positive") Long id
    ) {
        return PRODUCTS.stream()
            .filter(p -> p.get("id").equals(id.intValue()))
            .findFirst()
            .<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
