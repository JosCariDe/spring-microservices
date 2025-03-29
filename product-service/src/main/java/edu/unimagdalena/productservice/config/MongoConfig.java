package edu.unimagdalena.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.bson.types.Binary;
import java.util.UUID;
import java.util.Arrays;

@Configuration
@EnableMongoRepositories(basePackages = "edu.unimagdalena.productservice.repository")
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new BinaryToUUIDConverter(),
            new UUIDToBinaryConverter()
        ));
    }

    private static class BinaryToUUIDConverter implements Converter<Binary, UUID> {
        @Override
        public UUID convert(Binary source) {
            if (source == null) return null;
            return UUID.nameUUIDFromBytes(source.getData());
        }
    }

    private static class UUIDToBinaryConverter implements Converter<UUID, Binary> {
        @Override
        public Binary convert(UUID source) {
            if (source == null) return null;
            return new Binary(source.toString().getBytes());
        }
    }
} 