package com.cadencetune.api.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${processor.url:http://localhost:8000}")
  private String processorUrl;

  @Bean
  public RestClient processorRestClient() {
    return RestClient.builder()
        .baseUrl(processorUrl)
        .defaultHeader("Content-Type", "application/json")
        .build();
  }
}
