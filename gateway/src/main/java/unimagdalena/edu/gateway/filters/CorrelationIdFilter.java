package unimagdalena.edu.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("CorrelationIdFilter");
        String correlationId = UUID.randomUUID().toString();

        exchange.getRequest().mutate()
                .header("correlationId", correlationId)
                .build();

        exchange.getResponse().getHeaders().add("correlationId", correlationId);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 101; // Puedes ajustar la prioridad del filtro si hay otros filtros globales
    }
}
