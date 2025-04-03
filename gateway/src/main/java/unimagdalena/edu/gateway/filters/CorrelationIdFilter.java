package unimagdalena.edu.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class CorrelationIdFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Generar un UUID como correlationId
        String correlationId = UUID.randomUUID().toString();

        // Agregar el correlationId como cabecera en la solicitud
        exchange.getRequest().mutate()
                .header("correlationId", correlationId)
                .build();

        // Agregar el correlationId como cabecera en la respuesta
        exchange.getResponse().getHeaders().add("correlationId", correlationId);

        return chain.filter(exchange);
    }
}