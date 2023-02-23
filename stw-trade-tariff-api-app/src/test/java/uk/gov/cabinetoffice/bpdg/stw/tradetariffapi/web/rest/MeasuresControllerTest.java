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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties.CONTEXT_ROOT;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.application.InboundRequestMetrics;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.application.ResourceNameLabelResolver;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ConditionBasedRestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.MeasuresService;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.time.Clock;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.MeasuresRequest;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.MeasuresRequest.MeasuresRequestBuilder;

@WebFluxTest(
    controllers = MeasuresController.class,
    excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
class MeasuresControllerTest {

  @MockBean private MeasuresService measuresService;
  @MockBean private Clock clock;

  @MockBean private InboundRequestMetrics inboundRequestMetrics;
  @MockBean private ResourceNameLabelResolver resourceNameLabelResolver;

  @Autowired private WebTestClient webTestClient;

  LocalDate currentLocalDate = LocalDate.now();

  @BeforeEach
  public void setUp(){
    when(clock.currentLocalDate()).thenReturn(currentLocalDate);
  }

  @Test
  public void shouldReturnListOfMeasuresForACommodityWithoutAdditionalCode() {
    String commodityCode = "1234567890";
    UkCountry destinationCountry = UkCountry.GB;
    LocalDate importDate = LocalDate.now();
    TradeType tradeType = TradeType.IMPORT;
    String originCountry = "CN";

    MeasuresRequest request =
        MeasuresRequest.builder()
            .tradeType(tradeType)
            .dateOfTrade(importDate)
            .originCountry(originCountry)
            .destinationCountry(destinationCountry.name())
            .commodityCode(commodityCode)
            .build();

    MeasureOptions measureOptions1 =
        MeasureOptions.builder()
            .options(
                List.of(
                    DocumentCodeMeasureOption.builder()
                        .documentCodeDescription(
                            DocumentCodeDescription.builder()
                                .documentCode("123")
                                .descriptionOverlay("123 overlay")
                                .build())
                        .build()))
            .build();
    MeasureOptions measureOptions2 =
        MeasureOptions.builder()
            .options(
                List.of(
                    DocumentCodeMeasureOption.builder()
                        .documentCodeDescription(
                            DocumentCodeDescription.builder()
                                .documentCode("223")
                                .descriptionOverlay("223 overlay")
                                .build())
                        .build()))
            .build();
    given(measuresService.getMeasures(request))
        .willReturn(
            Flux.just(
                ConditionBasedRestrictiveMeasure.builder()
                    .id("1")
                    .descriptionOverlay("description 1")
                    .measureOptions(List.of(measureOptions1, measureOptions2))
                    .build()));

    webTestClient
        .get()
        .uri(createRequest(request))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.measures.length()")
        .isEqualTo(1)
        .jsonPath("$.measures[0].id")
        .isEqualTo("1")
        .jsonPath("$.measures[0].descriptionOverlay")
        .isEqualTo("description 1")
        .jsonPath("$.measures[0].measureOptions.length()")
        .isEqualTo("2")
        .jsonPath("$.measures[0].measureOptions[0].options.length()")
        .isEqualTo("1")
        .jsonPath("$.measures[0].measureOptions[0].options[0].certificateCode")
        .isEqualTo("123")
        .jsonPath("$.measures[0].measureOptions[0].options[0].descriptionOverlay")
        .isEqualTo("123 overlay")
        .jsonPath("$.measures[0].measureOptions[1].options.length()")
        .isEqualTo("1")
        .jsonPath("$.measures[0].measureOptions[1].options[0].certificateCode")
        .isEqualTo("223")
        .jsonPath("$.measures[0].measureOptions[1].options[0].descriptionOverlay")
        .isEqualTo("223 overlay");
  }

  @Test
  public void shouldReturnListOfMeasuresForACommodityWithAdditionalCode() {
    String commodityCode = "1234567890";
    UkCountry originCountry = UkCountry.GB;
    LocalDate exportDate = LocalDate.now();
    TradeType tradeType = TradeType.EXPORT;
    String destinationCountry = "CN";
    String additionalCode = "1234";

    MeasuresRequest request =
        MeasuresRequest.builder()
            .tradeType(tradeType)
            .dateOfTrade(exportDate)
            .originCountry(originCountry.name())
            .destinationCountry(destinationCountry)
            .commodityCode(commodityCode)
            .additionalCode(additionalCode)
            .build();

    MeasureOptions measureOptions1 =
        MeasureOptions.builder()
            .options(
                List.of(
                    DocumentCodeMeasureOption.builder()
                        .documentCodeDescription(
                            DocumentCodeDescription.builder()
                                .documentCode("123")
                                .descriptionOverlay("123 overlay")
                                .build())
                        .build()))
            .build();
    MeasureOptions measureOptions2 =
        MeasureOptions.builder()
            .options(
                List.of(
                    DocumentCodeMeasureOption.builder()
                        .documentCodeDescription(
                            DocumentCodeDescription.builder()
                                .documentCode("223")
                                .descriptionOverlay("223 overlay")
                                .build())
                        .build()))
            .build();
    given(measuresService.getMeasures(request))
        .willReturn(
            Flux.just(
                ConditionBasedRestrictiveMeasure.builder()
                    .id("1")
                    .descriptionOverlay("description 1")
                    .measureOptions(List.of(measureOptions1, measureOptions2))
                    .build()));

    webTestClient
        .get()
        .uri(createRequest(request))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.measures.length()")
        .isEqualTo(1)
        .jsonPath("$.measures[0].id")
        .isEqualTo("1")
        .jsonPath("$.measures[0].descriptionOverlay")
        .isEqualTo("description 1")
        .jsonPath("$.measures[0].measureOptions.length()")
        .isEqualTo("2")
        .jsonPath("$.measures[0].measureOptions[0].options.length()")
        .isEqualTo("1")
        .jsonPath("$.measures[0].measureOptions[0].options[0].certificateCode")
        .isEqualTo("123")
        .jsonPath("$.measures[0].measureOptions[0].options[0].descriptionOverlay")
        .isEqualTo("123 overlay")
        .jsonPath("$.measures[0].measureOptions[1].options.length()")
        .isEqualTo("1")
        .jsonPath("$.measures[0].measureOptions[1].options[0].certificateCode")
        .isEqualTo("223")
        .jsonPath("$.measures[0].measureOptions[1].options[0].descriptionOverlay")
        .isEqualTo("223 overlay");
  }

  @Test
  public void shouldReturnUseTodayAsTheDateOfTradeIfNotPassed() {
    String commodityCode = "1234567890";
    UkCountry destinationCountry = UkCountry.GB;
    TradeType tradeType = TradeType.IMPORT;
    String originCountry = "CN";
    String additionalCode = "1234";

    MeasuresRequestBuilder measuresRequestBuilder =
        MeasuresRequest.builder()
            .tradeType(tradeType)
            .originCountry(originCountry)
            .destinationCountry(destinationCountry.name())
            .commodityCode(commodityCode)
            .additionalCode(additionalCode);
    MeasuresRequest actualRequest = measuresRequestBuilder.build();
    MeasuresRequest expectedRequest = measuresRequestBuilder.dateOfTrade(currentLocalDate).build();

    MeasureOptions measureOptions1 =
        MeasureOptions.builder()
            .options(
                List.of(
                    DocumentCodeMeasureOption.builder()
                        .documentCodeDescription(
                            DocumentCodeDescription.builder()
                                .documentCode("123")
                                .descriptionOverlay("123 overlay")
                                .build())
                        .build()))
            .build();
    MeasureOptions measureOptions2 =
        MeasureOptions.builder()
            .options(
                List.of(
                    DocumentCodeMeasureOption.builder()
                        .documentCodeDescription(
                            DocumentCodeDescription.builder()
                                .documentCode("223")
                                .descriptionOverlay("223 overlay")
                                .build())
                        .build()))
            .build();

    ArgumentCaptor<MeasuresRequest> measuresRequestArgumentCaptor =
        ArgumentCaptor.forClass(MeasuresRequest.class);
    given(measuresService.getMeasures(measuresRequestArgumentCaptor.capture()))
        .willReturn(
            Flux.just(
                ConditionBasedRestrictiveMeasure.builder()
                    .id("1")
                    .descriptionOverlay("description 1")
                    .measureOptions(List.of(measureOptions1, measureOptions2))
                    .build()));

    webTestClient
        .get()
        .uri(createRequest(actualRequest))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.measures.length()")
        .isEqualTo(1)
        .jsonPath("$.measures[0].id")
        .isEqualTo("1");

    assertThat(measuresRequestArgumentCaptor.getValue()).isEqualTo(expectedRequest);
  }

  @Test
  void shouldReturnErrorIfDestinationCountryPassedIsNotAValidUKCountryForImports(){
    MeasuresRequestBuilder measuresRequestBuilder =
        MeasuresRequest.builder()
            .tradeType(TradeType.IMPORT)
            .originCountry("CN")
            .destinationCountry("HK")
            .commodityCode("1234567890");
    MeasuresRequest actualRequest = measuresRequestBuilder.build();

    webTestClient
        .get()
        .uri(createRequest(actualRequest))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.validationErrors.length()")
        .isEqualTo(1)
        .jsonPath("$.validationErrors[0].fieldName")
        .isEqualTo("destinationCountry")
        .jsonPath("$.validationErrors[0].message")
        .isEqualTo("Destination country HK is not a valid UK country");
  }

  @Test
  void shouldReturnErrorIfOriginCountryPassedIsNotAValidUKCountryForExports(){
    MeasuresRequestBuilder measuresRequestBuilder =
        MeasuresRequest.builder()
            .tradeType(TradeType.EXPORT)
            .originCountry("CN")
            .destinationCountry("HK")
            .commodityCode("1234567890");
    MeasuresRequest actualRequest = measuresRequestBuilder.build();

    webTestClient
        .get()
        .uri(createRequest(actualRequest))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.validationErrors.length()")
        .isEqualTo(1)
        .jsonPath("$.validationErrors[0].fieldName")
        .isEqualTo("originCountry")
        .jsonPath("$.validationErrors[0].message")
        .isEqualTo("Origin country CN is not a valid UK country");
  }

  private Function<UriBuilder, URI> createRequest(MeasuresRequest request) {
    return builder -> {
      UriBuilder uriBuilder =
          builder
              .path(
                  String.format(
                      CONTEXT_ROOT + "/v1/commodities/%s/restrictive-measures", request.getCommodityCode()))
              .queryParam("tradeType", request.getTradeType())
              .queryParam("originCountry", request.getOriginCountry())
              .queryParam("destinationCountry", request.getDestinationCountry());

      if (Objects.nonNull(request.getDateOfTrade())) {
        uriBuilder.queryParam(
            "tradeDate",
            String.format(
                "%tY-%tm-%td",
                request.getDateOfTrade(), request.getDateOfTrade(), request.getDateOfTrade()));
      }
      if (Objects.nonNull(request.getAdditionalCode())) {
        uriBuilder.queryParam("additionalCode", request.getAdditionalCode());
      }
      return uriBuilder.build();
    };
  }
}
