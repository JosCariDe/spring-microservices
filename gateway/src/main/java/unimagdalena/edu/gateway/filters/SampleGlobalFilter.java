package unimagdalena.edu.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SampleGlobalFilter implements GlobalFilter {

    private final Logger logger = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //PRE
        logger.info("Ejecutando global filter antes del requuest PRE");

        

        //POST
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("Ejecutando global filter requuest POST");

            exchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "red").build());
            //exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        }));
    }
}
