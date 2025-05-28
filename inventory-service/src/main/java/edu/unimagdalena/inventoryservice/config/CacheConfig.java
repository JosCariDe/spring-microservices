package edu.unimagdalena.inventoryservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

//@Configuration
//@EnableCaching
public class CacheConfig {

    public static final String INVENTORY_CACHE = "inventory-cache";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(INVENTORY_CACHE, createConfig(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private static RedisCacheConfiguration createConfig(Duration duration) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(duration);
    }
}
