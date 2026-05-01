package co.com.bancolombia.usecase;

import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
public class SaludoUseCase {

    public Mono<String> execute(String name) {
        return Mono.fromCallable(() -> {
            if (name == null || name.trim().isEmpty()) {
                log.warning("Intento de saludo con nombre vacío");
                return "¡Hola! ¿Cómo te llamas?";
            }
            String greeting = String.format(
                    "¡Hola %s! Bienvenido al servidor MCP de Bancolombia. ¿En qué puedo ayudarte hoy?",
                    name.trim());
            log.info(String.format("Saludo generado para: %s", name));
            return greeting;
        });
    }
}

