package unimagdalena.edu.gateway.caching;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GlobalCacheFilter implements WebFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final Duration cacheDuration;
    private final boolean cacheOnlyGet;

    public GlobalCacheFilter(RedisTemplate<String, String> redisTemplate, Duration cacheDuration, boolean cacheOnlyGet) {
        this.redisTemplate = redisTemplate;
        this.cacheDuration = cacheDuration;
        this.cacheOnlyGet = cacheOnlyGet;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!shouldCache(exchange)) {
            return chain.filter(exchange);
        }

        String cacheKey = generateCacheKey(exchange);
        String cachedResponse = redisTemplate.opsForValue().get(cacheKey);

        if (cachedResponse != null) {
            byte[] bytes = cachedResponse.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

            exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            exchange.getResponse().getHeaders().set(HttpHeaders.CACHE_CONTROL, "public, max-age=" + cacheDuration.getSeconds());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> captureResponse(exchange, cacheKey))
                .onErrorResume(throwable -> {
                    // En caso de error, invalidamos el caché si existe
                    redisTemplate.delete(cacheKey);
                    return Mono.error(throwable);
                });
    }

    private boolean shouldCache(ServerWebExchange exchange) {
        if (cacheOnlyGet && !exchange.getRequest().getMethod().equals(HttpMethod.GET)) {
            return false;
        }
        return true;
    }

    public void captureResponse(ServerWebExchange exchange, String cacheKey) {
        exchange.getResponse().beforeCommit(() -> {
            return DataBufferUtils.join((Publisher<? extends DataBuffer>) exchange.getResponse().bufferFactory().allocateBuffer()) // Crear un buffer vacio
                    .doOnNext(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        String responseBody = new String(content, StandardCharsets.UTF_8);
                        redisTemplate.opsForValue().set(cacheKey, responseBody);
                    })
                    .then();
        });
    }

    private String generateCacheKey(ServerWebExchange exchange) {
        return "CACHE:" + exchange.getRequest().getURI().toString();
    }
}
