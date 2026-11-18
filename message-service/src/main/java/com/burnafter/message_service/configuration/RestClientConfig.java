package com.burnafter.message_service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * Without timeouts, a request in flight when the audit service dies can hang
     * indefinitely: delivery is a sequential loop, so the whole outbox of that
     * instance stalls, and the circuit breaker never opens because the call
     * never fails. Timeouts turn "hanging" into "failing", which the retry and
     * circuit breaker logic can actually react to.
     */
    @Bean
    public RestClient auditRestClient(
            @Value("${AUDIT_BASE_URL}") String baseUrl,
            @Value("${audit.connect-timeout-ms:1000}") long connectTimeoutMs,
            @Value("${audit.read-timeout-ms:2000}") long readTimeoutMs) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
