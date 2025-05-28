package unimagdalena.edu.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/actuator/**", "/health/**", "/eureka/**").permitAll()
                        .pathMatchers("/login", "/error", "/webjars/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }
}