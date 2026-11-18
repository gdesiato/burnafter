package com.burnafter.message_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h
                        .referrerPolicy(r -> r.policy(ReferrerPolicy.NO_REFERRER))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                )
                .authorizeHttpRequests(auth -> auth
                        // Health checks + Prometheus scraping (not exposed via Nginx).
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pastes").permitAll()
                        // Read links are public by design: the random UUID is the
                        // capability and the decryption key lives in the URL fragment.
                        .requestMatchers(HttpMethod.GET, "/api/pastes/*/data").permitAll()
                        .anyRequest().denyAll()
                );

        return http.build();
    }
}
