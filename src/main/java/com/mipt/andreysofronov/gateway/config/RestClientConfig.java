package com.mipt.andreysofronov.gateway.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Builds the {@link RestClient} the gateway uses to call the external tasks service (lecture 10):
 * configured base URL, connect + read timeouts, and default {@code User-Agent} / {@code Accept}
 * headers. The read timeout is what makes the {@code mode=timeout} scenario abort instead of
 * hanging.
 */
@Configuration
public class RestClientConfig {

  @Bean
  public RestClient externalApiRestClient(
      @Value("${external.api.base-url}") String baseUrl,
      @Value("${external.api.connect-timeout-ms:2000}") long connectTimeoutMs,
      @Value("${external.api.read-timeout-ms:2000}") long readTimeoutMs,
      @Value("${external.api.user-agent:resilient-secure-gateway/1.0}") String userAgent) {

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
    requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
