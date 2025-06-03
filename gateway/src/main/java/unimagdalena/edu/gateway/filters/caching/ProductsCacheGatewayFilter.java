package unimagdalena.edu.gateway.filters.caching;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Component
public class ProductsCacheGatewayFilter extends AbstractGatewayFilterFactory<ProductsCacheGatewayFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(ProductsCacheGatewayFilter.class);
    private static final String CACHE_PREFIX = "products:cache:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    public ProductsCacheGatewayFilter(ReactiveStringRedisTemplate redisTemplate,
                                      ObjectMapper objectMapper,
                                      MeterRegistry meterRegistry) {

        super(Config.class);
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.cacheHitCounter = Counter.builder("gateway_cache_hits")
                .description("Number of cache hits")
                .tag("service", "products")
                .register(meterRegistry);
        this.cacheMissCounter = Counter.builder("gateway_cache_misses")
                .description("Number of cache misses")
                .tag("service", "products")
                .register(meterRegistry);
    }

    @Override
    public GatewayFilter apply(Config config) {
        logger.info("Configuring gateway filter");
        return (exchange, chain) -> {
            String method = exchange.getRequest().getMethod().name();
            String path = exchange.getRequest().getPath().value();

            // Solo cachear requests GET
            if (!"GET".equals(method)) {
                return chain.filter(exchange);
            }

            // Solo cachear rutas específicas de productos
            if (!shouldCache(path)) {
                return chain.filter(exchange);
            }

            String cacheKey = generateCacheKey(exchange.getRequest().getURI().toString());

            logger.debug("Checking cache for key: {}", cacheKey);

            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                    return Flux.from(body)
                            .buffer()
                            .flatMap(dataBuffers -> {
                                DataBuffer combined = bufferFactory.join(dataBuffers);
                                String responseBody = combined.toString(StandardCharsets.UTF_8);

                                if (originalResponse.getStatusCode().is2xxSuccessful()) {
                                    redisTemplate.opsForValue()
                                            .set(cacheKey, responseBody, config.getTtl())
                                            .doOnSuccess(result -> logger.debug("Cached response for key: {}", cacheKey))
                                            .doOnError(error -> logger.error("Error caching response: ", error))
                                            .subscribe();
                                }

                                originalResponse.getHeaders().add("X-Cache", "MISS");

                                return Mono.just(combined);
                            })
                            .flatMap(dataBuffer -> super.writeWith(Mono.just(dataBuffer)))
                            .then();
                }
            };

            // Mutate the exchange to use the decorated response
            org.springframework.web.server.ServerWebExchange mutatedExchange = exchange.mutate().response(decoratedResponse).build();

            // Intentar obtener del cache
            return redisTemplate.opsForValue().get(cacheKey)
                    .cast(String.class)
                    .flatMap(cachedResponse -> {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        cacheHitCounter.increment();
                        return createCachedResponse(mutatedExchange, cachedResponse); // Use mutatedExchange
                    })
                    .switchIfEmpty(
                        // Cache MISS - continue with original request and cache response
                        Mono.fromRunnable(() -> {
                            logger.info("Cache MISS for key: {}", cacheKey);
                            cacheMissCounter.increment();
                        }).then(
                            chain.filter(mutatedExchange) // Use mutatedExchange
                        )
                    );
        };
    }

    private boolean shouldCache(String path) {
        // Definir qué rutas de productos cachear
        return
                path.matches(".*/api/products"); // GET /api/products/category/{category}
    }

    private String generateCacheKey(String uri) {
        return CACHE_PREFIX + uri.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private Mono<Void> createCachedResponse(org.springframework.web.server.ServerWebExchange exchange, String cachedData) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add("X-Cache", "HIT");

        DataBuffer buffer = response.bufferFactory().wrap(cachedData.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("ttl");
    }

    public static class Config {
        private Duration ttl = DEFAULT_TTL;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
