/*
 * Copyright 2021 Crown Copyright (Single Trade Window)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.TradeTariffApiConfig;

@Configuration
public class Resilience4JConfiguration {

  public static final String OTT_API_CIRCUIT_BREAKER_NAME = "tradeTariffApi";

  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(
      final CircuitBreakerConfig circuitBreakerConfig,
      final CircuitBreakerRegistry circuitBreakerRegistry,
      final ApplicationProperties applicationProperties) {
    TradeTariffApiConfig tradeTariffApiConfig = applicationProperties.getTradeTariffApi();
    Duration durationForRetryBackOff =
        tradeTariffApiConfig
            .getRetryMinBackoff()
            .multipliedBy(tradeTariffApiConfig.getRetryMaxAttempt());
    // to cater for jitter and to make sure circuit breaker does not kick off earlier
    Duration durationForRetryBackOffWithBuffer =
        durationForRetryBackOff.plus(tradeTariffApiConfig.getRetryMinBackoff());

    Duration totalTimeToWaitForRetryToComplete =
        tradeTariffApiConfig
            .getTimeout()
            .multipliedBy(tradeTariffApiConfig.getRetryMaxAttempt())
            .plus(durationForRetryBackOffWithBuffer);
    return factory -> {
      factory.configureDefault(
          id ->
              new Resilience4JConfigBuilder(id)
                  .timeLimiterConfig(
                      TimeLimiterConfig.custom()
                          .timeoutDuration(totalTimeToWaitForRetryToComplete)
                          .build())
                  .circuitBreakerConfig(circuitBreakerConfig)
                  .build());
      factory.configureCircuitBreakerRegistry(circuitBreakerRegistry);
    };
  }

  @Bean
  public ReactiveCircuitBreaker reactiveCircuitBreaker(
      ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory) {
    return reactiveResilience4JCircuitBreakerFactory.create(OTT_API_CIRCUIT_BREAKER_NAME);
  }

  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry(
      final CircuitBreakerConfig circuitBreakerConfig,
      final CircuitBreakerProperties circuitBreakerProperties) {
    var circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
    if (!circuitBreakerProperties.isEnable()) {
      circuitBreakerRegistry
          .circuitBreaker(OTT_API_CIRCUIT_BREAKER_NAME)
          .transitionToDisabledState();
    }
    return circuitBreakerRegistry;
  }

  @Bean
  public CircuitBreakerConfig circuitBreakerConfig(
      final CircuitBreakerProperties circuitBreakerProperties) {
    return CircuitBreakerConfig.custom()
        .slidingWindowType(
            SlidingWindowType.valueOf(circuitBreakerProperties.getSlidingWindowType()))
        .minimumNumberOfCalls(circuitBreakerProperties.getMinimumNumberOfCalls())
        .slidingWindowSize(circuitBreakerProperties.getSlidingWindowSize())
        .waitDurationInOpenState(circuitBreakerProperties.getWaitDurationInOpenState())
        .permittedNumberOfCallsInHalfOpenState(
            circuitBreakerProperties.getPermittedNumberOfCallsInHalfOpenState())
        .failureRateThreshold(circuitBreakerProperties.getFailureRateThreshold())
        .build();
  }
}
