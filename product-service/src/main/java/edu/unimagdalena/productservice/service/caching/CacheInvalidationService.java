package edu.unimagdalena.productservice.service.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CacheInvalidationService {

    private static final Logger logger = LoggerFactory.getLogger(CacheInvalidationService.class);
    private static final String CACHE_PREFIX = "products:cache:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public CacheInvalidationService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> invalidateProductCache(Long productId) {
        String pattern = CACHE_PREFIX + "*api_products_" + productId + "*";
        return invalidateCacheByPattern(pattern)
                .doOnSuccess(v -> logger.info("Invalidated cache for product: {}", productId));
    }

    public Mono<Void> invalidateAllProductsCache() {
        String pattern = CACHE_PREFIX + "*api_products*";
        return invalidateCacheByPattern(pattern)
                .doOnSuccess(v -> logger.info("Invalidated all products cache"));
    }

    public Mono<Void> invalidateCategoryCache(String category) {
        String pattern = CACHE_PREFIX + "*category_" + category + "*";
        return invalidateCacheByPattern(pattern)
                .doOnSuccess(v -> logger.info("Invalidated cache for category: {}", category));
    }

    private Mono<Void> invalidateCacheByPattern(String pattern) {
        return redisTemplate.keys(pattern)
                .flatMap(redisTemplate::delete)
                .then()
                .doOnError(error -> logger.error("Error invalidating cache: ", error))
                .onErrorResume(error -> Mono.empty());
    }
}