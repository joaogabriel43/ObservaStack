package io.observastack.demo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller de demonstração para endpoints de Pedidos.
 *
 * <p>Simula criação e consulta de pedidos para fins de demonstração
 * da instrumentação do ObservaStack SDK.</p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /** Store in-memory para demonstração (sem banco de dados neste módulo) */
    private final ConcurrentHashMap<String, Map<String, Object>> ordersStore = new ConcurrentHashMap<>();
    private final AtomicInteger orderCounter = new AtomicInteger(1);

    /**
     * Cria um novo pedido.
     *
     * @param request corpo da requisição com productId e quantity
     * @return HTTP 201 com o pedido criado
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        int orderNumber = orderCounter.getAndIncrement();

        Map<String, Object> order = Map.of(
            "orderId", orderId,
            "orderNumber", orderNumber,
            "productId", request.productId(),
            "quantity", request.quantity(),
            "status", "PENDING",
            "createdAt", Instant.now().toString(),
            "totalPrice", new BigDecimal("49.90").multiply(BigDecimal.valueOf(request.quantity()))
        );

        ordersStore.put(orderId, order);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Busca um pedido pelo ID.
     *
     * @param orderId UUID do pedido
     * @return HTTP 200 com o pedido, ou HTTP 404 se não encontrado
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String orderId) {
        Map<String, Object> order = ordersStore.get(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * Record interno para deserialização do body da requisição de criação de pedido.
     */
    public record OrderRequest(
        @Positive(message = "productId must be positive") long productId,
        @Positive(message = "quantity must be positive") int quantity,
        @NotBlank(message = "customerName must not be blank") String customerName
    ) {}
}
