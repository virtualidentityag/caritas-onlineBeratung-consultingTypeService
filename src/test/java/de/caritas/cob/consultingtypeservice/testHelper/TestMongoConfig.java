package de.caritas.cob.consultingtypeservice.testHelper;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMongoConfig {

  @Bean
  public HealthIndicator mongoHealthIndicator() {
    return () -> Health.up().build();
  }
}
