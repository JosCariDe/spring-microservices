package unimagdalena.edu.gateway.filters;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Component
public class RedisCacheResponseFilter extends AbstractGatewayFilterFactory<RedisCacheResponseFilter.Config> {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisCacheResponseFilter(@Qualifier("reactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String cacheKey = generateCacheKey(exchange.getRequest().getURI().getPath());
            Duration ttl = Duration.parse("PT" + config.getTtl());

            return redisTemplate.opsForValue().get(cacheKey)
                    .flatMap(cachedResponse -> {
                        byte[] bytes = cachedResponse.getBytes(StandardCharsets.UTF_8);
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                    })
                    .switchIfEmpty(
                            chain.filter(exchange.mutate().response(decorateResponse(exchange.getResponse(), cacheKey, ttl)).build())
                    );
        };
    }

    private ServerHttpResponse decorateResponse(ServerHttpResponse response, String cacheKey, Duration ttl) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return super.writeWith(Flux.from(body)
                        .collectList()
                        .flatMap(dataBuffers -> {
                            byte[] bytes = new byte[0];
                            for (DataBuffer buffer : dataBuffers) {
                                byte[] bufferBytes = new byte[buffer.readableByteCount()];
                                buffer.read(bufferBytes);
                                DataBufferUtils.release(buffer);
                                
                                byte[] newBytes = new byte[bytes.length + bufferBytes.length];
                                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                                System.arraycopy(bufferBytes, 0, newBytes, bytes.length, bufferBytes.length);
                                bytes = newBytes;
                            }
                            
                            String responseBody = new String(bytes, StandardCharsets.UTF_8);
                            return redisTemplate.opsForValue()
                                    .set(cacheKey, responseBody, ttl)
                                    .then(Mono.just(dataBuffers));
                        })
                        .flatMapMany(Flux::fromIterable));
            }
        };
    }

    private String generateCacheKey(String path) {
        return "response-cache:" + path;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("ttl");
    }

    public static class Config {
        private String ttl;

        public String getTtl() {
            return ttl;
        }

        public void setTtl(String ttl) {
            this.ttl = ttl;
        }
    }
}