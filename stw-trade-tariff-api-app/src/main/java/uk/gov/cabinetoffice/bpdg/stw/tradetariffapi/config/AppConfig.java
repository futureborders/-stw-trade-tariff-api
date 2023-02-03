// Copyright 2021 Crown Copyright (Single Trade Window)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.util.Metrics;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.TradeTariffApi;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.config.MetricsConfig;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.downstream.DownstreamEndpointLabelNameResolver;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.downstream.DownstreamRequestMetrics;

@Configuration
@Import({MetricsConfig.class})
public class AppConfig {

  private final ApplicationProperties applicationProperties;
  private final DownstreamEndpointLabelNameResolver downstreamEndpointLabelNameResolver;
  private final DownstreamRequestMetrics downstreamRequestMetrics;

  @Value("${STW_SIGNPOSTING_API_MAX_MEMORY_BUFFER_SIZE:5}")
  private Integer maxInMemorySize; // in MBs

  @Autowired
  public AppConfig(
      ApplicationProperties applicationProperties,
      DownstreamEndpointLabelNameResolver downstreamEndpointLabelNameResolver,
      DownstreamRequestMetrics downstreamRequestMetrics,
      MeterRegistry meterRegistry) {
    this.applicationProperties = applicationProperties;
    this.downstreamEndpointLabelNameResolver = downstreamEndpointLabelNameResolver;
    this.downstreamRequestMetrics = downstreamRequestMetrics;
    Metrics.MicrometerConfiguration.useRegistry(meterRegistry);
  }

  @Bean
  TradeTariffApi tradeTariffApi(ReactiveCircuitBreaker reactiveCircuitBreaker) {
    return new TradeTariffApi(
        WebClient.builder()
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs(
                        configurer ->
                            configurer
                                .defaultCodecs()
                                .maxInMemorySize(maxInMemorySize * 1024 * 1000))
                    .build()),
        applicationProperties.getTradeTariffApi(),
        downstreamRequestMetrics,
        downstreamEndpointLabelNameResolver,
        reactiveCircuitBreaker);
  }

  @Bean
  public WebClient webClient() {
    var httpClient =
        HttpClient.create()
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                (int) applicationProperties.getContentApi().getTimeout().toMillis())
            .doOnConnected(
                c ->
                    c.addHandlerLast(
                        new ReadTimeoutHandler(
                            (int) applicationProperties.getContentApi().getTimeout().toSeconds())));
    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }
}
