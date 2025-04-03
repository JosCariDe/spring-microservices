package unimagdalena.edu.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

public class ProductCacheFilter implements GatewayFilter {

    // Caché en memoria usando ConcurrentHashMap
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String cacheKey = exchange.getRequest().getURI().getPath();
        
        // Verificar si la respuesta está en caché
        String cachedResponse = cache.get(cacheKey);
        if (cachedResponse != null) {
            // Respuesta encontrada en caché (HIT)
            exchange.getResponse().getHeaders().add("X-CACHE", "HIT");
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(cachedResponse.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
        
        // Respuesta no encontrada en caché (MISS)
        exchange.getResponse().getHeaders().add("X-CACHE", "MISS");
        
        // Interceptar la respuesta para almacenarla en caché
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Aquí podríamos implementar la lógica para almacenar en caché
            // pero para simplificar, vamos a usar un valor de ejemplo
            cache.put(cacheKey, "{\"message\": \"Producto en caché\"}");
        }));
    }
}