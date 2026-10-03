package com.cadencetune.api.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${processor.url:http://localhost:8000}")
  private String processorUrl;

  // 타임아웃을 별도 설정으로 분리하여 환경별 조정 가능성을 열어둔다.
  @Value("${processor.connect-timeout:5000}")
  private int connectTimeoutMs;

  @Value("${processor.read-timeout:10000}")
  private int readTimeoutMs;

  @Bean
  public RestClient processorRestClient() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeoutMs);
    factory.setReadTimeout(readTimeoutMs);

    return RestClient.builder()
        .baseUrl(processorUrl)
        .defaultHeader("Content-Type", "application/json")
        .requestFactory(factory)
        .build();
  }
}
