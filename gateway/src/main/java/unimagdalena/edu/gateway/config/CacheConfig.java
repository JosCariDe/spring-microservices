package unimagdalena.edu.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import unimagdalena.edu.gateway.caching.GlobalCacheFilter;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public GlobalCacheFilter globalCacheFilter(RedisTemplate<String, String> redisTemplate) {
        // Configuración por defecto:
        // - Duración del caché: 5 minutos
        // - Solo cachear peticiones GET
        return new GlobalCacheFilter(redisTemplate, Duration.ofMinutes(5), true);
    }
} 