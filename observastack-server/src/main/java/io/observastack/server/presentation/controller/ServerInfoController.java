package io.observastack.server.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller de boas-vindas do ObservaStack Server.
 * Expõe endpoint raiz para verificação básica de disponibilidade.
 */
@RestController
@RequestMapping("/api/v1")
public class ServerInfoController {

    @GetMapping
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
            "service", "observastack-server",
            "version", "0.1.0-SNAPSHOT",
            "status", "UP"
        ));
    }
}
