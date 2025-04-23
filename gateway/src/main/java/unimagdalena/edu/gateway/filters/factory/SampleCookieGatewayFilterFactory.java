package unimagdalena.edu.gateway.filters.factory;

import org.mapstruct.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class SampleCookieGatewayFilterFactory extends AbstractGatewayFilterFactory<SampleCookieGatewayFilterFactory.ConfigurationCookie> {

    private final Logger logger = LoggerFactory.getLogger(SampleCookieGatewayFilterFactory.class);

    public SampleCookieGatewayFilterFactory() {
        super(ConfigurationCookie.class);
    }

    @Override
    public GatewayFilter apply(ConfigurationCookie config) {
        return (exchange, chain) -> {

            logger.info("Ejecutando el gateway filter factory: " + config.message);

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                Optional.ofNullable(config.value).ifPresent(cookie -> {
                    exchange.getResponse().addCookie(ResponseCookie.from(config.name, cookie).build());
                });
                logger.info("Ejecutando el gateway filter factory: " + config.message);
            }));
        };
    }

    public static class ConfigurationCookie {
        private String name;
        private String value;
        private String message;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
