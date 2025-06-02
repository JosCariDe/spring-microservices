package unimagdalena.edu.gateway.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import unimagdalena.edu.gateway.filters.SampleGlobalFilter;

@RestController
public class FallbackController {

    private final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/fallback")
    public Mono<ResponseEntity<String>> fallback() {
        logger.info("FallbackController: fallback CIRCUITBREACKER");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("{\"error\": \"Servicio temporalmente no disponible. Intente más tarde CIRCUITBREACKER ABIERTO\"}.\"}"));
    }

}
