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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties.CONTEXT_ROOT;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.application.InboundRequestMetrics;
import uk.gov.cabinetoffice.bpdg.stw.monitoring.prometheus.metrics.application.ResourceNameLabelResolver;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.AdditionalCodesService;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.time.Clock;

@WebFluxTest(
    controllers = AdditionalCodesController.class,
    excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
class AdditionalCodesControllerTest {

  @Autowired private WebTestClient webTestClient;

  @MockBean private AdditionalCodesService additionalCodesService;
  @MockBean private Clock clock;

  @MockBean private InboundRequestMetrics inboundRequestMetrics;
  @MockBean private ResourceNameLabelResolver resourceNameLabelResolver;

  AdditionalCodesControllerTest() {}

  @Nested
  class GetAdditionalCodes {

    private final String commodityCode = "1006101000";
    private final String originCountry = "CN";
    private final UkCountry destinationCountry = UkCountry.GB;

    @BeforeEach
    void setUp() {
      when(resourceNameLabelResolver.getResourceName(anyString(), anyString()))
          .thenReturn(Optional.of("apiCall"));
    }

    @Test
    @DisplayName("happy path - with import date")
    void shouldReturnAdditionalCodesWithImportDate() {

      AdditionalCode additionalCode1 =
          AdditionalCode.builder()
              .code("1111")
              .description("Additional 1 code description")
              .build();
      AdditionalCode additionalCode2 =
          AdditionalCode.builder()
              .code("1112")
              .description("Additional 2 code description")
              .build();

      var importDate = LocalDate.of(2021, 10, 30);
      when(additionalCodesService.getAdditionalCodes(
              commodityCode,
              TradeType.IMPORT,
              originCountry,
              destinationCountry.name(),
              importDate))
          .thenReturn(Mono.just(List.of(additionalCode1, additionalCode2)));

      webTestClient
          .get()
          .uri(
              createRequest(
                  commodityCode, TradeType.IMPORT, "CN", UkCountry.GB.name(), importDate, null))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.length()")
          .isEqualTo(2)
          .jsonPath("$.data[0].code")
          .isEqualTo("1111")
          .jsonPath("$.data[0].description")
          .isEqualTo("Additional 1 code description")
          .jsonPath("$.data[1].code")
          .isEqualTo("1112")
          .jsonPath("$.data[1].description")
          .isEqualTo("Additional 2 code description");
    }

    @Test
    @DisplayName("happy path - with trade date")
    void shouldReturnAdditionalCodesWithDateOfTrade() {

      AdditionalCode additionalCode1 =
          AdditionalCode.builder()
              .code("1111")
              .description("Additional 1 code description")
              .build();
      AdditionalCode additionalCode2 =
          AdditionalCode.builder()
              .code("1112")
              .description("Additional 2 code description")
              .build();

      var tradeDate = LocalDate.of(2021, 10, 30);
      when(additionalCodesService.getAdditionalCodes(
              commodityCode, TradeType.IMPORT, originCountry, destinationCountry.name(), tradeDate))
          .thenReturn(Mono.just(List.of(additionalCode1, additionalCode2)));

      webTestClient
          .get()
          .uri(
              createRequest(
                  commodityCode, TradeType.IMPORT, "CN", UkCountry.GB.name(), null, tradeDate))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.length()")
          .isEqualTo(2)
          .jsonPath("$.data[0].code")
          .isEqualTo("1111")
          .jsonPath("$.data[0].description")
          .isEqualTo("Additional 1 code description")
          .jsonPath("$.data[1].code")
          .isEqualTo("1112")
          .jsonPath("$.data[1].description")
          .isEqualTo("Additional 2 code description");
    }

    @Test
    @DisplayName("happy path - with trade date - for exports")
    void shouldReturnAdditionalCodesWithDateOfTradeForExport() {
      String commodityCode = "1006101000";
      String destinationCountry = "CN";
      UkCountry originCountry = UkCountry.XI;

      AdditionalCode additionalCode1 =
          AdditionalCode.builder()
              .code("1111")
              .description("Additional 1 code description")
              .build();
      AdditionalCode additionalCode2 =
          AdditionalCode.builder()
              .code("1112")
              .description("Additional 2 code description")
              .build();

      var tradeDate = LocalDate.of(2021, 10, 30);
      when(additionalCodesService.getAdditionalCodes(
              commodityCode, TradeType.EXPORT, originCountry.name(), destinationCountry, tradeDate))
          .thenReturn(Mono.just(List.of(additionalCode1, additionalCode2)));

      webTestClient
          .get()
          .uri(
              createRequest(
                  commodityCode,
                  TradeType.EXPORT,
                  originCountry.name(),
                  destinationCountry,
                  null,
                  tradeDate))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.length()")
          .isEqualTo(2)
          .jsonPath("$.data[0].code")
          .isEqualTo("1111")
          .jsonPath("$.data[0].description")
          .isEqualTo("Additional 1 code description")
          .jsonPath("$.data[1].code")
          .isEqualTo("1112")
          .jsonPath("$.data[1].description")
          .isEqualTo("Additional 2 code description");
    }

    @Test
    @DisplayName("happy path - with today as trade date")
    void shouldReturnAdditionalCodesWithNoImportDateOrTradeDate() {

      AdditionalCode additionalCode1 =
          AdditionalCode.builder()
              .code("1111")
              .description("Additional 1 code description")
              .build();
      AdditionalCode additionalCode2 =
          AdditionalCode.builder()
              .code("1112")
              .description("Additional 2 code description")
              .build();
      var today = LocalDate.now();
      when(clock.currentLocalDate()).thenReturn(today);
      when(additionalCodesService.getAdditionalCodes(
              commodityCode, TradeType.IMPORT, originCountry, destinationCountry.name(), today))
          .thenReturn(Mono.just(List.of(additionalCode1, additionalCode2)));

      webTestClient
          .get()
          .uri(
              createRequest(commodityCode, TradeType.IMPORT, "CN", UkCountry.GB.name(), null, null))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.length()")
          .isEqualTo(2)
          .jsonPath("$.data[0].code")
          .isEqualTo("1111")
          .jsonPath("$.data[0].description")
          .isEqualTo("Additional 1 code description")
          .jsonPath("$.data[1].code")
          .isEqualTo("1112")
          .jsonPath("$.data[1].description")
          .isEqualTo("Additional 2 code description");
    }

    @Test
    @DisplayName("happy path - when commodity code do not exist")
    void shouldReturn404WhenCommodityCodeDoesNotExist() {
      var importDate = LocalDate.now();
      when(additionalCodesService.getAdditionalCodes(
              eq(commodityCode),
              eq(TradeType.IMPORT),
              eq(originCountry),
              eq(destinationCountry.name()),
              any(LocalDate.class)))
          .thenThrow(new ResourceNotFoundException("Commodity", commodityCode));

      webTestClient
          .get()
          .uri(
              createRequest(
                  commodityCode, TradeType.IMPORT, "CN", UkCountry.GB.name(), importDate, null))
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.message")
          .isEqualTo("Resource 'Commodity' not found with id '1006101000'");
    }

    @Test
    @DisplayName("happy path - when additional codes do not exist")
    void shouldReturn404WhenAdditionalCodesDoNotExist() {
      var importDate = LocalDate.now();
      when(additionalCodesService.getAdditionalCodes(
              eq(commodityCode),
              eq(TradeType.IMPORT),
              eq(originCountry),
              eq(destinationCountry.name()),
              any(LocalDate.class)))
          .thenReturn(Mono.just(Collections.emptyList()));

      webTestClient
          .get()
          .uri(
              createRequest(
                  commodityCode, TradeType.IMPORT, "CN", UkCountry.GB.name(), importDate, null))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.length()")
          .isEqualTo(0);
    }

    @Test
    void shouldReturnErrorIfDestinationCountryPassedIsNotAValidUKCountryForImports() {
      webTestClient
          .get()
          .uri(createRequest(commodityCode, TradeType.IMPORT, "CN", "HK", null, null))
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
    void shouldReturnErrorIfOriginCountryPassedIsNotAValidUKCountryForExports() {
      webTestClient
          .get()
          .uri(createRequest(commodityCode, TradeType.EXPORT, "HK", "CN", null, null))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody()
          .jsonPath("$.validationErrors.length()")
          .isEqualTo(1)
          .jsonPath("$.validationErrors[0].fieldName")
          .isEqualTo("originCountry")
          .jsonPath("$.validationErrors[0].message")
          .isEqualTo("Origin country HK is not a valid UK country");
    }

    @Test
    void shouldReturnErrorIfInvalidDateFormat() {
      webTestClient
        .get()
        .uri(createRequest(commodityCode, TradeType.IMPORT, "CN", "GB", "09-10-2022"))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.validationErrors.length()")
        .isEqualTo(1)
        .jsonPath("$.validationErrors[0].fieldName")
        .isEqualTo("tradeDate")
        .jsonPath("$.validationErrors[0].message")
        .isEqualTo("Text '09-10-2022' could not be parsed at index 0");
    }

    @Test
    void shouldReturnErrorIfInvalidCommodityCode() {
      webTestClient
        .get()
        .uri(createRequest("1212812182182812", TradeType.IMPORT, "CN", "GB", "2022-10-09"))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.validationErrors.length()")
        .isEqualTo(1)
        .jsonPath("$.validationErrors[0].fieldName")
        .isEqualTo("commodityCode")
        .jsonPath("$.validationErrors[0].message")
        .isEqualTo("must match \"^\\d{8}|\\d{10}$\"");
    }

    @Test
    void shouldReturnErrorIfNullTradeType() {
      webTestClient
        .get()
        .uri(createRequest("1212812182182812", null, "CN", "GB", "2022-10-09"))
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.validationErrors.length()")
        .isEqualTo(2)
        .jsonPath("$.validationErrors[*].message").value((List messages)->
           assertThat(messages).containsExactlyInAnyOrder("must match \"^\\d{8}|\\d{10}$\"","must not be null")
          );

    }

    private Function<UriBuilder, URI> createRequest(
        String commodityCode,
        TradeType tradeType,
        String originCountry,
        String destinationCountry,
        LocalDate importDate,
        LocalDate tradeDate) {
      return builder -> {
        UriBuilder uriBuilder =
            builder
                .path(
                    String.format(CONTEXT_ROOT + "/commodities/%s/additional-codes", commodityCode))
                .queryParam("tradeType", tradeType)
                .queryParam("originCountry", originCountry)
                .queryParam("destinationCountry", destinationCountry);

        return Objects.nonNull(importDate)
            ? uriBuilder
                .queryParam(
                    "importDate", String.format("%tY-%tm-%td", importDate, importDate, importDate))
                .build()
            : Objects.nonNull(tradeDate)
                ? uriBuilder
                    .queryParam(
                        "tradeDate", String.format("%tY-%tm-%td", tradeDate, tradeDate, tradeDate))
                    .build()
                : uriBuilder.build();
      };
    }

    private Function<UriBuilder, URI> createRequest(
      String commodityCode,
      TradeType tradeType,
      String originCountry,
      String destinationCountry,
      String tradeDate) {
      return builder -> {
        UriBuilder uriBuilder =
          builder
            .path(
              String.format(CONTEXT_ROOT + "/commodities/%s/additional-codes", commodityCode))
            .queryParam("tradeType", tradeType)
            .queryParam("originCountry", originCountry)
            .queryParam("destinationCountry", destinationCountry)
            .queryParam("tradeDate",tradeDate);
        return uriBuilder.build();
      };
    }
  }
}
