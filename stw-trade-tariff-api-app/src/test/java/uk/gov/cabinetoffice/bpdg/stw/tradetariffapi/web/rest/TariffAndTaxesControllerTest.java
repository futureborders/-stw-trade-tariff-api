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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties.CONTEXT_ROOT;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Quota;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tariff;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TariffAndTaxes;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tax;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.TariffAndTaxesService;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.TariffAndTaxesRequest;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.TariffAndTaxesRequest.TariffAndTaxesRequestBuilder;

@WebFluxTest(
    controllers = TariffAndTaxesController.class,
    excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
class TariffAndTaxesControllerTest {

  @Autowired private WebTestClient webTestClient;
  @MockBean private TariffAndTaxesService tariffAndTaxesService;

  @MockBean private InboundRequestMetrics inboundRequestMetrics;
  @MockBean private ResourceNameLabelResolver resourceNameLabelResolver;

  @BeforeEach
  void setUp() {
    when(resourceNameLabelResolver.getResourceName(anyString(), anyString()))
        .thenReturn(Optional.of("apiCall"));
  }

  @ParameterizedTest
  @EnumSource(Locale.class)
  void
      shouldGetAllDetailsOfATariffWithOrderingBasedOnMeasureTypeIdGeographicalAreaAdditionalNumberAndQuotaNumber(
          Locale locale) {
    TariffAndTaxesRequest request =
        TariffAndTaxesRequest.builder()
            .commodityCode("1006101000")
            .tradeType(TradeType.IMPORT)
            .originCountry("CN")
            .destinationCountry(UkCountry.GB)
            .importDate(LocalDate.now())
            .locale(locale)
            .build();

    String tariffText1 = "tariff text1";
    String tariffText2 = "tariff text2";
    String tariffText3 = "tariff text3";
    String tariffValue = "0.00%";
    String additionalCode = "ADD_CODE";
    String additionalCode2 = "ADD_CODE2";
    String additionalCodeDescription = "Additional code desc";
    String geographicalAreaId1 = "1001";
    String geographicalAreaId2 = "MA";
    String geographicalAreaId3 = "1001";
    String geographicalAreaDescription = "1001 description";
    String quotaNumber = "1001001";
    String quotaNumber2 = "1001002";

    TariffAndTaxes tariffAndTaxes =
        TariffAndTaxes.builder()
            .duties(
                List.of(
                    Tariff.builder()
                        .measureTypeId("104")
                        .text(tariffText1)
                        .value(tariffValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(
                            GeographicalArea.builder()
                                .id(geographicalAreaId1)
                                .description(geographicalAreaDescription)
                                .build())
                        .quota(Quota.builder().number(quotaNumber).build())
                        .build(),
                    Tariff.builder()
                        .measureTypeId("104")
                        .text(tariffText2)
                        .value(tariffValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(
                            GeographicalArea.builder()
                                .id(geographicalAreaId2)
                                .description(geographicalAreaDescription)
                                .build())
                        .quota(Quota.builder().number(quotaNumber).build())
                        .build(),
                    Tariff.builder()
                        .measureTypeId("104")
                        .text(tariffText2)
                        .value(tariffValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode2)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(
                            GeographicalArea.builder()
                                .id(geographicalAreaId2)
                                .description(geographicalAreaDescription)
                                .build())
                        .quota(Quota.builder().number(quotaNumber).build())
                        .build(),
                    Tariff.builder()
                        .measureTypeId("104")
                        .text(tariffText2)
                        .value(tariffValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode2)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(
                            GeographicalArea.builder()
                                .id(geographicalAreaId2)
                                .description(geographicalAreaDescription)
                                .build())
                        .quota(Quota.builder().number(quotaNumber2).build())
                        .build(),
                    Tariff.builder()
                        .measureTypeId("103")
                        .text(tariffText3)
                        .value(tariffValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(
                            GeographicalArea.builder()
                                .id(geographicalAreaId3)
                                .description(geographicalAreaDescription)
                                .build())
                        .quota(Quota.builder().number(quotaNumber).build())
                        .build()))
            .build();
    when(tariffAndTaxesService.getTariffAndTaxes(request)).thenReturn(Mono.just(tariffAndTaxes));

    webTestClient
        .get()
        .uri(createRequest(request))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.tariffs.length()")
        .isEqualTo(5)
        .jsonPath("$.taxes.length()")
        .isEqualTo(0)
        .jsonPath("$.tariffs[0].text")
        .isEqualTo(tariffText3)
        .jsonPath("$.tariffs[0].value")
        .isEqualTo(tariffValue)
        .jsonPath("$.tariffs[0].additionalCode.code")
        .isEqualTo(additionalCode)
        .jsonPath("$.tariffs[0].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.tariffs[0].geographicalArea.id")
        .isEqualTo(geographicalAreaId3)
        .jsonPath("$.tariffs[0].geographicalArea.description")
        .isEqualTo(geographicalAreaDescription)
        .jsonPath("$.tariffs[0].quota.number")
        .isEqualTo(quotaNumber)
        .jsonPath("$.tariffs[1].text")
        .isEqualTo(tariffText1)
        .jsonPath("$.tariffs[1].value")
        .isEqualTo(tariffValue)
        .jsonPath("$.tariffs[1].additionalCode.code")
        .isEqualTo(additionalCode)
        .jsonPath("$.tariffs[1].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.tariffs[1].geographicalArea.id")
        .isEqualTo(geographicalAreaId1)
        .jsonPath("$.tariffs[1].geographicalArea.description")
        .isEqualTo(geographicalAreaDescription)
        .jsonPath("$.tariffs[1].quota.number")
        .isEqualTo(quotaNumber)
        .jsonPath("$.tariffs[2].text")
        .isEqualTo(tariffText2)
        .jsonPath("$.tariffs[2].value")
        .isEqualTo(tariffValue)
        .jsonPath("$.tariffs[2].additionalCode.code")
        .isEqualTo(additionalCode)
        .jsonPath("$.tariffs[2].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.tariffs[2].geographicalArea.id")
        .isEqualTo(geographicalAreaId2)
        .jsonPath("$.tariffs[2].geographicalArea.description")
        .isEqualTo(geographicalAreaDescription)
        .jsonPath("$.tariffs[2].quota.number")
        .isEqualTo(quotaNumber)
        .jsonPath("$.tariffs[3].text")
        .isEqualTo(tariffText2)
        .jsonPath("$.tariffs[3].value")
        .isEqualTo(tariffValue)
        .jsonPath("$.tariffs[3].additionalCode.code")
        .isEqualTo(additionalCode2)
        .jsonPath("$.tariffs[3].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.tariffs[3].geographicalArea.id")
        .isEqualTo(geographicalAreaId2)
        .jsonPath("$.tariffs[3].geographicalArea.description")
        .isEqualTo(geographicalAreaDescription)
        .jsonPath("$.tariffs[3].quota.number")
        .isEqualTo(quotaNumber)
        .jsonPath("$.tariffs[4].text")
        .isEqualTo(tariffText2)
        .jsonPath("$.tariffs[4].value")
        .isEqualTo(tariffValue)
        .jsonPath("$.tariffs[4].additionalCode.code")
        .isEqualTo(additionalCode2)
        .jsonPath("$.tariffs[4].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.tariffs[4].geographicalArea.id")
        .isEqualTo(geographicalAreaId2)
        .jsonPath("$.tariffs[4].geographicalArea.description")
        .isEqualTo(geographicalAreaDescription)
        .jsonPath("$.tariffs[4].quota.number")
        .isEqualTo(quotaNumber2);
  }

  @ParameterizedTest
  @EnumSource(Locale.class)
  void shouldGetAllDetailsOfTaxesWithOrderingBasedOnMeasureTypeIdGeographicalAreaAndAdditionalCode(
      Locale locale) {
    TariffAndTaxesRequest request =
        TariffAndTaxesRequest.builder()
            .commodityCode("1006101000")
            .tradeType(TradeType.IMPORT)
            .originCountry("CN")
            .destinationCountry(UkCountry.GB)
            .importDate(LocalDate.now())
            .locale(locale)
            .build();

    String dutyText1 = "duty text1";
    String dutyText2 = "duty text2";
    String dutyText3 = "duty text3";
    String dutyValue = "0.00%";
    String additionalCode = "ADD_CODE";
    String additionalCode2 = "ADD_CODE2";
    String additionalCodeDescription = "Additional code desc";

    TariffAndTaxes tariffAndTaxes =
        TariffAndTaxes.builder()
            .duties(
                List.of(
                    Tax.builder()
                        .measureTypeId("104")
                        .text(dutyText1)
                        .value(dutyValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(GeographicalArea.builder().id("MA").build())
                        .build(),
                    Tax.builder()
                        .measureTypeId("104")
                        .text(dutyText2)
                        .value(dutyValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(GeographicalArea.builder().id("1011").build())
                        .build(),
                    Tax.builder()
                        .measureTypeId("104")
                        .text(dutyText2)
                        .value(dutyValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode2)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(GeographicalArea.builder().id("1011").build())
                        .build(),
                    Tax.builder()
                        .measureTypeId("103")
                        .text(dutyText3)
                        .value(dutyValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(GeographicalArea.builder().id("1011").build())
                        .build()))
            .build();
    when(tariffAndTaxesService.getTariffAndTaxes(request)).thenReturn(Mono.just(tariffAndTaxes));

    webTestClient
        .get()
        .uri(createRequest(request))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.tariffs.length()")
        .isEqualTo(0)
        .jsonPath("$.taxes.length()")
        .isEqualTo(4)
        .jsonPath("$.taxes[0].text")
        .isEqualTo(dutyText3)
        .jsonPath("$.taxes[0].value")
        .isEqualTo(dutyValue)
        .jsonPath("$.taxes[0].additionalCode.code")
        .isEqualTo(additionalCode)
        .jsonPath("$.taxes[0].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.taxes[1].text")
        .isEqualTo(dutyText2)
        .jsonPath("$.taxes[1].value")
        .isEqualTo(dutyValue)
        .jsonPath("$.taxes[1].additionalCode.code")
        .isEqualTo(additionalCode)
        .jsonPath("$.taxes[1].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.taxes[2].text")
        .isEqualTo(dutyText2)
        .jsonPath("$.taxes[2].value")
        .isEqualTo(dutyValue)
        .jsonPath("$.taxes[2].additionalCode.code")
        .isEqualTo(additionalCode2)
        .jsonPath("$.taxes[2].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.taxes[3].text")
        .isEqualTo(dutyText1)
        .jsonPath("$.taxes[3].value")
        .isEqualTo(dutyValue)
        .jsonPath("$.taxes[3].additionalCode.code")
        .isEqualTo(additionalCode)
        .jsonPath("$.taxes[3].additionalCode.description")
        .isEqualTo(additionalCodeDescription);
  }

  @ParameterizedTest
  @EnumSource(Locale.class)
  void shouldSetTariffAndDuty(Locale locale) {
    TariffAndTaxesRequest request =
        TariffAndTaxesRequest.builder()
            .commodityCode("1006101000")
            .tradeType(TradeType.IMPORT)
            .originCountry("CN")
            .destinationCountry(UkCountry.GB)
            .importDate(LocalDate.now())
            .locale(locale)
            .build();

    String tariffText = "tariff text";
    String tariffValue = "0.00%";
    String additionalCode = "ADD_CODE";
    String additionalCodeDescription = "Additional code desc";
    String geographicalAreaId = "1001";
    String geographicalAreaDescription = "1001 description";
    String quotaNumber = "1001001";

    String dutyText = "duty text";
    String dutyValue = "0.00%";
    String dutyAdditionalCode = "ADD1_CODE";
    String dutyAdditionalCodeDescription = "Additional code1 desc";

    TariffAndTaxes tariffAndTaxes =
        TariffAndTaxes.builder()
            .duties(
                List.of(
                    Tariff.builder()
                        .text(tariffText)
                        .value(tariffValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(additionalCode)
                                .description(additionalCodeDescription)
                                .build())
                        .geographicalArea(
                            GeographicalArea.builder()
                                .id(geographicalAreaId)
                                .description(geographicalAreaDescription)
                                .build())
                        .quota(Quota.builder().number(quotaNumber).build())
                        .build(),
                    Tax.builder()
                        .text(dutyText)
                        .value(dutyValue)
                        .additionalCode(
                            AdditionalCode.builder()
                                .code(dutyAdditionalCode)
                                .description(dutyAdditionalCodeDescription)
                                .build())
                        .build()))
            .build();
    when(tariffAndTaxesService.getTariffAndTaxes(request)).thenReturn(Mono.just(tariffAndTaxes));

    webTestClient
        .get()
        .uri(createRequest(request))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.tariffs.length()")
        .isEqualTo(1)
        .jsonPath("$.tariffs[0].text")
        .isEqualTo(tariffText)
        .jsonPath("$.tariffs[0].value")
        .isEqualTo(tariffValue)
        .jsonPath("$.tariffs[0].additionalCode.code")
        .isEqualTo(additionalCode)
        .jsonPath("$.tariffs[0].additionalCode.description")
        .isEqualTo(additionalCodeDescription)
        .jsonPath("$.tariffs[0].geographicalArea.id")
        .isEqualTo(geographicalAreaId)
        .jsonPath("$.tariffs[0].geographicalArea.description")
        .isEqualTo(geographicalAreaDescription)
        .jsonPath("$.tariffs[0].quota.number")
        .isEqualTo(quotaNumber)
        .jsonPath("$.taxes.length()")
        .isEqualTo(1)
        .jsonPath("$.taxes[0].text")
        .isEqualTo(dutyText)
        .jsonPath("$.taxes[0].value")
        .isEqualTo(dutyValue)
        .jsonPath("$.taxes[0].additionalCode.code")
        .isEqualTo(dutyAdditionalCode)
        .jsonPath("$.taxes[0].additionalCode.description")
        .isEqualTo(dutyAdditionalCodeDescription);
  }

  @Test
  void shouldUseTodayAsTheDateOfTradeAndEnglishLocaleIfNotPassed() {
    TariffAndTaxesRequestBuilder tariffAndTaxesRequestBuilder =
        TariffAndTaxesRequest.builder()
            .commodityCode("1006101000")
            .tradeType(TradeType.IMPORT)
            .originCountry("CN")
            .destinationCountry(UkCountry.GB);
    TariffAndTaxesRequest request = tariffAndTaxesRequestBuilder.build();
    TariffAndTaxesRequest expectedRequest =
        tariffAndTaxesRequestBuilder.locale(Locale.EN).importDate(LocalDate.now()).build();

    ArgumentCaptor<TariffAndTaxesRequest> TariffAndTaxesRequestArgumentCaptor =
        ArgumentCaptor.forClass(TariffAndTaxesRequest.class);
    TariffAndTaxes tariffAndTaxes = TariffAndTaxes.builder().duties(Collections.emptyList()).build();
    when(tariffAndTaxesService.getTariffAndTaxes(TariffAndTaxesRequestArgumentCaptor.capture()))
        .thenReturn(Mono.just(tariffAndTaxes));

    webTestClient
        .get()
        .uri(createRequest(request))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.tariffs.length()")
        .isEqualTo(0);

    assertThat(TariffAndTaxesRequestArgumentCaptor.getValue()).isEqualTo(expectedRequest);
  }

  private Function<UriBuilder, URI> createRequest(TariffAndTaxesRequest request) {
    return builder -> {
      UriBuilder uriBuilder =
          builder
              .path(
                  String.format(
                      CONTEXT_ROOT + "/commodities/%s/duties", request.getCommodityCode()))
              .queryParam("tradeType", request.getTradeType())
              .queryParam("originCountry", request.getOriginCountry())
              .queryParam("destinationCountry", request.getDestinationCountry());
      if (request.getImportDate() != null) {
        uriBuilder.queryParam(
            "importDate",
            String.format(
                "%tY-%tm-%td",
                request.getImportDate(), request.getImportDate(), request.getImportDate()));
      }
      if (request.getLocale() != null) {
        uriBuilder.queryParam("locale", request.getLocale().name());
      }
      return uriBuilder.build();
    };
  }
}
