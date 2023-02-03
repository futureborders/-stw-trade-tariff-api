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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.Timer;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.application.InboundRequestMetrics;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.application.ResourceNameLabelResolver;

@Component
@Slf4j
public class MetricRecordingFilter implements WebFilter {

  private final InboundRequestMetrics inboundRequestMetrics;
  private final ResourceNameLabelResolver resourceNameLabelResolver;

  @Autowired
  public MetricRecordingFilter(
      InboundRequestMetrics inboundRequestMetrics,
      ResourceNameLabelResolver resourceNameLabelResolver) {
    this.inboundRequestMetrics = inboundRequestMetrics;
    this.resourceNameLabelResolver = resourceNameLabelResolver;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
    final ServerHttpRequest request = serverWebExchange.getRequest();
    final Optional<String> resourceName = getResourceName(request);
    var timer = Timer.startNew();
    serverWebExchange
        .getResponse()
        .beforeCommit(
            () -> {
              resourceName.ifPresent(
                  r -> {
                    log.debug(
                        "Request parts {}-{}, mapped to API name {}",
                        request.getMethod(),
                        request.getPath(),
                        resourceName.orElse("unknown"));
                    final Optional<Integer> responseStatusCode =
                        Optional.ofNullable(serverWebExchange.getResponse().getRawStatusCode());
                    responseStatusCode.ifPresent(
                        status -> {
                          inboundRequestMetrics.incrementResponseCount(resourceName.get(), status);
                          log.debug("Response was {}", status);
                        });
                    inboundRequestMetrics.recordResponseLatency(resourceName.get(), timer.end());
                  });
              return Mono.empty();
            });
    return webFilterChain.filter(serverWebExchange);
  }

  private Optional<String> getResourceName(ServerHttpRequest request) {
    String requestUri = request.getURI().getPath();
    String method = Objects.requireNonNull(request.getMethod()).name();
    return resourceNameLabelResolver.getResourceName(method, requestUri);
  }
}
