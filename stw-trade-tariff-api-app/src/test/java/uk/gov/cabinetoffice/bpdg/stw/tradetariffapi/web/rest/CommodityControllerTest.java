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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties.CONTEXT_ROOT;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityMeasures;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Header;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingHeaderContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingStepResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TaxAndDuty;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.CommodityService;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.error.ErrorResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.error.ValidationError;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.error.ValidationErrorResponse;

@WebFluxTest(
    controllers = CommodityController.class,
    excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
public class CommodityControllerTest {

  @Autowired private WebTestClient webTestClient;
  @MockBean private CommodityService commodityService;

  @MockBean private InboundRequestMetrics inboundRequestMetrics;
  @MockBean private ResourceNameLabelResolver resourceNameLabelResolver;

  @BeforeAll
  public static void setup() {
    Locale.setDefault(Locale.ENGLISH);
  }

  @Nested
  class GetCommodityMeasures {

    @BeforeEach
    void setUp() {
      when(resourceNameLabelResolver.getResourceName(anyString(), anyString()))
          .thenReturn(Optional.of("apiCall"));
    }

    @Test
    @DisplayName(
        "happy path-with query params commodityCode, userType, tradeType and originCountry")
    void shouldReturnCommodityMeasuresWhenNoImportDate() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.DECLARING_TRADER)
              .originCountry("CN")
              .importDate(LocalDate.now())
              .destinationCountry(UkCountry.GB)
              .build();

      TaxAndDuty taxAndDuty = TaxAndDuty.builder().applicable(true).build();
      CommodityMeasures commodityMeasuresResponse =
          CommodityMeasures.builder()
              .commodityCode(request.getCommodityCode())
              .commodityDescription("For sowing")
              .measures(
                  List.of(
                      Measure.builder()
                          .measureType(MeasureType.builder().description("Measure Type 1").build())
                          .applicableTradeTypes(List.of(request.getTradeType()))
                          .geographicalArea(
                              GeographicalArea.builder()
                                  .id(request.getOriginCountry())
                                  .description("China")
                                  .build())
                          .build()))
              .taxAndDuty(taxAndDuty)
              .build();

      when(commodityService.getCommodityMeasures(request))
          .thenReturn(Mono.just(commodityMeasuresResponse));

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.commodityCode")
          .isEqualTo(request.getCommodityCode())
          .jsonPath("$.commodityDescription")
          .isEqualTo("For sowing")
          .jsonPath("$.measures.length()")
          .isEqualTo(1)
          .jsonPath("$.measures[0].measureType.description")
          .isEqualTo("Measure Type 1")
          .jsonPath("$.measures[0].tradeType")
          .isEqualTo(TradeType.IMPORT.name())
          .jsonPath("$.measures[0].geographicalArea.id")
          .isEqualTo(request.getOriginCountry())
          .jsonPath("$.measures[0].geographicalArea.description")
          .isEqualTo("China")
          .jsonPath("$.taxAndDuty.applicable")
          .isEqualTo("true");
    }

    @Test
    @DisplayName(
        "happy path-with query params commodityCode, userType, tradeType, originCountry and import date")
    void shouldReturnCommodityMeasuresWhenImportDateExists() {
      var importDate = LocalDate.now();
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.DECLARING_TRADER)
              .originCountry("CN")
              .destinationCountry(UkCountry.GB)
              .importDate(importDate)
              .build();

      TaxAndDuty taxAndDuty = TaxAndDuty.builder().applicable(true).build();
      CommodityMeasures commodityMeasuresResponse =
          CommodityMeasures.builder()
              .commodityCode(request.getCommodityCode())
              .commodityDescription("For sowing")
              .measures(
                  List.of(
                      Measure.builder()
                          .measureType(MeasureType.builder().description("Measure Type 1").build())
                          .applicableTradeTypes(List.of(request.getTradeType()))
                          .geographicalArea(
                              GeographicalArea.builder()
                                  .id(request.getOriginCountry())
                                  .description("China")
                                  .build())
                          .build()))
              .taxAndDuty(taxAndDuty)
              .build();

      when(commodityService.getCommodityMeasures(any(CommodityMeasuresRequest.class)))
          .thenReturn(Mono.just(commodityMeasuresResponse));

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.commodityCode")
          .isEqualTo(request.getCommodityCode())
          .jsonPath("$.commodityDescription")
          .isEqualTo("For sowing")
          .jsonPath("$.measures.length()")
          .isEqualTo(1)
          .jsonPath("$.measures[0].measureType.description")
          .isEqualTo("Measure Type 1")
          .jsonPath("$.measures[0].tradeType")
          .isEqualTo(TradeType.IMPORT.name())
          .jsonPath("$.measures[0].geographicalArea.id")
          .isEqualTo(request.getOriginCountry())
          .jsonPath("$.measures[0].geographicalArea.description")
          .isEqualTo("China")
          .jsonPath("$.taxAndDuty.applicable")
          .isEqualTo("true");
    }

    @Test
    @DisplayName("super headers, headers and signposting steps")
    void shouldReturnSuperHeaderAndHeaderAndRelatedSignpostingSteps() {
      var importDate = LocalDate.now();
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.DECLARING_TRADER)
              .originCountry("CN")
              .destinationCountry(UkCountry.GB)
              .importDate(importDate)
              .build();

      SuperHeader superHeader1 =
          SuperHeader.builder()
              .description("super header 1")
              .explanatoryText("super header 1 explanatory text")
              .orderIndex(1)
              .build();
      Header header1 =
          Header.builder()
              .id(1)
              .description("header 1 desc")
              .orderIndex(1)
              .linkText("header 1 link text")
              .explanatoryText("header 1 explanatory text")
              .relatedTo("IMPORT_RECORD_KEEPING")
              .build();
      Header header2 =
          Header.builder()
              .id(2)
              .description("header 2 desc")
              .orderIndex(2)
              .linkText("header 2 link text")
              .explanatoryText("header 2 explanatory text")
              .externalLink("external link")
              .relatedTo("IMPORT_DECLARATION")
              .build();
      SignpostingStepResponse signpostingStep1 =
          SignpostingStepResponse.builder()
              .id(1)
              .stepUrl("step 1 url")
              .stepDescription("step 1 desc")
              .stepHowtoDescription("step 1 how to")
              .agentContent("step 1 agent content")
              .declaringTraderContent("step 1 dec agent")
              .nonDeclaringTraderContent("step 1 non dec agent")
              .build();
      SignpostingStepResponse signpostingStep2 =
          SignpostingStepResponse.builder()
              .id(2)
              .stepUrl("step 2 url")
              .stepDescription("step 2 desc")
              .stepHowtoDescription("step 2 how to")
              .agentContent("step 2 agent content")
              .declaringTraderContent("step 2 dec agent")
              .nonDeclaringTraderContent("step 2 non dec agent")
              .build();
      SignpostingStepResponse signpostingStep3 =
          SignpostingStepResponse.builder()
              .id(3)
              .stepUrl("step 3 url")
              .stepDescription("step 3 desc")
              .stepHowtoDescription("step 3 how to")
              .agentContent("step 3 agent content")
              .declaringTraderContent("step 3 dec agent")
              .nonDeclaringTraderContent("step 3 non dec agent")
              .build();
      CommodityMeasures commodityMeasuresResponse =
          CommodityMeasures.builder()
              .signpostingContents(
                  List.of(
                      SignpostingContent.builder()
                          .superHeader(superHeader1)
                          .headers(
                              List.of(
                                  SignpostingHeaderContent.builder()
                                      .header(header1)
                                      .steps(List.of(signpostingStep1, signpostingStep2))
                                      .build(),
                                  SignpostingHeaderContent.builder()
                                      .header(header2)
                                      .steps(List.of(signpostingStep3))
                                      .build()))
                          .build()))
              .build();

      when(commodityService.getCommodityMeasures(any(CommodityMeasuresRequest.class)))
          .thenReturn(Mono.just(commodityMeasuresResponse));

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.signpostingContents.length()")
          .isEqualTo(1)
          .jsonPath("$.signpostingContents[0].superHeader.orderIndex")
          .isEqualTo(superHeader1.getOrderIndex())
          .jsonPath("$.signpostingContents[0].superHeader.description")
          .isEqualTo(superHeader1.getDescription())
          .jsonPath("$.signpostingContents[0].superHeader.explanatoryText")
          .isEqualTo(superHeader1.getExplanatoryText())
          .jsonPath("$.signpostingContents[0].headers.length()")
          .isEqualTo(2)
          .jsonPath("$.signpostingContents[0].headers[0].header.id")
          .isEqualTo(header1.getId())
          .jsonPath("$.signpostingContents[0].headers[0].header.orderIndex")
          .isEqualTo(header1.getOrderIndex())
          .jsonPath("$.signpostingContents[0].headers[0].header.description")
          .isEqualTo(header1.getDescription())
          .jsonPath("$.signpostingContents[0].headers[0].header.explanatoryText")
          .isEqualTo(header1.getExplanatoryText())
          .jsonPath("$.signpostingContents[0].headers[0].header.linkText")
          .isEqualTo(header1.getLinkText())
          .jsonPath("$.signpostingContents[0].headers[0].header.relatedTo")
          .isEqualTo(header1.getRelatedTo())
          .jsonPath("$.signpostingContents[0].headers[0].steps.length()")
          .isEqualTo(2)
          .jsonPath("$.signpostingContents[0].headers[0].steps[0].id")
          .isEqualTo(signpostingStep1.getId())
          .jsonPath("$.signpostingContents[0].headers[0].steps[0].stepDescription")
          .isEqualTo(signpostingStep1.getStepDescription())
          .jsonPath("$.signpostingContents[0].headers[0].steps[0].stepHowtoDescription")
          .isEqualTo(signpostingStep1.getStepHowtoDescription())
          .jsonPath("$.signpostingContents[0].headers[0].steps[0].stepUrl")
          .isEqualTo(signpostingStep1.getStepUrl())
          .jsonPath("$.signpostingContents[0].headers[0].steps[0].agentContent")
          .isEqualTo(signpostingStep1.getAgentContent())
          .jsonPath("$.signpostingContents[0].headers[0].steps[0].declaringTraderContent")
          .isEqualTo(signpostingStep1.getDeclaringTraderContent())
          .jsonPath("$.signpostingContents[0].headers[0].steps[0].nonDeclaringTraderContent")
          .isEqualTo(signpostingStep1.getNonDeclaringTraderContent())
          .jsonPath("$.signpostingContents[0].headers[0].steps[1].id")
          .isEqualTo(signpostingStep2.getId())
          .jsonPath("$.signpostingContents[0].headers[0].steps[1].stepDescription")
          .isEqualTo(signpostingStep2.getStepDescription())
          .jsonPath("$.signpostingContents[0].headers[0].steps[1].stepHowtoDescription")
          .isEqualTo(signpostingStep2.getStepHowtoDescription())
          .jsonPath("$.signpostingContents[0].headers[0].steps[1].stepUrl")
          .isEqualTo(signpostingStep2.getStepUrl())
          .jsonPath("$.signpostingContents[0].headers[0].steps[1].agentContent")
          .isEqualTo(signpostingStep2.getAgentContent())
          .jsonPath("$.signpostingContents[0].headers[0].steps[1].declaringTraderContent")
          .isEqualTo(signpostingStep2.getDeclaringTraderContent())
          .jsonPath("$.signpostingContents[0].headers[0].steps[1].nonDeclaringTraderContent")
          .isEqualTo(signpostingStep2.getNonDeclaringTraderContent())
          .jsonPath("$.signpostingContents[0].headers[1].header.id")
          .isEqualTo(header2.getId())
          .jsonPath("$.signpostingContents[0].headers[1].header.orderIndex")
          .isEqualTo(header2.getOrderIndex())
          .jsonPath("$.signpostingContents[0].headers[1].header.description")
          .isEqualTo(header2.getDescription())
          .jsonPath("$.signpostingContents[0].headers[1].header.explanatoryText")
          .isEqualTo(header2.getExplanatoryText())
          .jsonPath("$.signpostingContents[0].headers[1].header.linkText")
          .isEqualTo(header2.getLinkText())
          .jsonPath("$.signpostingContents[0].headers[1].header.relatedTo")
          .isEqualTo(header2.getRelatedTo())
          .jsonPath("$.signpostingContents[0].headers[1].header.externalLink")
          .isEqualTo(header2.getExternalLink())
          .jsonPath("$.signpostingContents[0].headers[1].steps.length()")
          .isEqualTo(1)
          .jsonPath("$.signpostingContents[0].headers[1].steps[0].id")
          .isEqualTo(signpostingStep3.getId())
          .jsonPath("$.signpostingContents[0].headers[1].steps[0].stepDescription")
          .isEqualTo(signpostingStep3.getStepDescription())
          .jsonPath("$.signpostingContents[0].headers[1].steps[0].stepHowtoDescription")
          .isEqualTo(signpostingStep3.getStepHowtoDescription())
          .jsonPath("$.signpostingContents[0].headers[1].steps[0].stepUrl")
          .isEqualTo(signpostingStep3.getStepUrl())
          .jsonPath("$.signpostingContents[0].headers[1].steps[0].agentContent")
          .isEqualTo(signpostingStep3.getAgentContent())
          .jsonPath("$.signpostingContents[0].headers[1].steps[0].declaringTraderContent")
          .isEqualTo(signpostingStep3.getDeclaringTraderContent())
          .jsonPath("$.signpostingContents[0].headers[1].steps[0].nonDeclaringTraderContent")
          .isEqualTo(signpostingStep3.getNonDeclaringTraderContent());
    }

    @Test
    @DisplayName(
        "8 digits for export is accepted and should call trade tariff api with 10 digits by appending two zeroes")
    void export_with_8_digits_should_return_200() {

      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("10061010")
              .tradeType(TradeType.EXPORT)
              .userType(UserType.NON_DECLARING_TRADER)
              .originCountry("CN")
              .importDate(LocalDate.now())
              .destinationCountry(UkCountry.GB)
              .build();

      CommodityMeasures commodityMeasuresResponse =
          CommodityMeasures.builder()
              .commodityCode(request.getCommodityCode())
              .commodityDescription("For sowing")
              .measures(
                  List.of(
                      Measure.builder()
                          .measureType(MeasureType.builder().description("Measure Type 1").build())
                          .applicableTradeTypes(List.of(request.getTradeType()))
                          .geographicalArea(
                              GeographicalArea.builder()
                                  .id(request.getOriginCountry())
                                  .description("China")
                                  .build())
                          .build()))
              .build();

      CommodityMeasuresRequest requestWithTwoZeroesAppendedToCommodityCode =
          CommodityMeasuresRequest.builder()
              .commodityCode(request.getCommodityCode() + "00")
              .tradeType(request.getTradeType())
              .userType(request.getUserType())
              .originCountry(request.getOriginCountry())
              .importDate(LocalDate.now())
              .destinationCountry(request.getDestinationCountry())
              .build();

      when(commodityService.getCommodityMeasures(requestWithTwoZeroesAppendedToCommodityCode))
          .thenReturn(Mono.just(commodityMeasuresResponse));

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.commodityCode")
          .isEqualTo(request.getCommodityCode());
    }

    @Test
    @DisplayName("should throw 404 if commodity not found")
    void unknown_commodity() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.NON_DECLARING_TRADER)
              .originCountry("CN")
              .importDate(LocalDate.now())
              .destinationCountry(UkCountry.GB)
              .build();

      when(commodityService.getCommodityMeasures(request))
          .thenReturn(
              Mono.error(new ResourceNotFoundException("Commodity", request.getCommodityCode())));

      ErrorResponse errorResponse =
          ErrorResponse.builder()
              .message("Resource 'Commodity' not found with id '1006101000'")
              .build();

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody(ErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if country parameter is more than 2 digits")
    void country_with_more_than_2_digits() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.NON_DECLARING_TRADER)
              .originCountry("ABC")
              .destinationCountry(UkCountry.GB)
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("must match \"^\\w{2}$\"")
                          .fieldName("originCountry")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if commodity code has more than 10 digits")
    void commodity_with_more_than_10_digits() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("10061010001")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.NON_DECLARING_TRADER)
              .originCountry("FR")
              .destinationCountry(UkCountry.GB)
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("must match \"^\\d{8}|\\d{10}$\"")
                          .fieldName("commodityCode")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if commodity code contains non digit character")
    void commodity_with_non_digit_character() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("100610100p")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.NON_DECLARING_TRADER)
              .originCountry("FR")
              .destinationCountry(UkCountry.GB)
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("must match \"^\\d{8}|\\d{10}$\"")
                          .fieldName("commodityCode")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if trade type is not import/export")
    void unknown_trade_type() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .originCountry("CN")
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("TradeType 'UNKNOWN' does not exist")
                          .fieldName("tradeType")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequestWithUnknownTradeType(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if trade type is missing")
    void missing_country() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .originCountry("CN")
              .destinationCountry(UkCountry.GB)
              .userType(UserType.NON_DECLARING_TRADER)
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("must not be null")
                          .fieldName("tradeType")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if user type is not trader/intermediary")
    void unknown_user_type() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .originCountry("CN")
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("UserType 'UNKNOWN' does not exist")
                          .fieldName("userType")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequestWithUnknownUserType(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if destination country passed is not recognised")
    void not_recognised_destination_country() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .userType(UserType.NON_DECLARING_TRADER)
              .originCountry("CN")
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("Invalid destination country 'UNKNOWN'")
                          .fieldName("destinationCountry")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequestWithUnrecognisedDestinationCountry(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 400 if user type is missing")
    void missing_user_type() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .originCountry("CN")
              .destinationCountry(UkCountry.GB)
              .tradeType(TradeType.IMPORT)
              .build();

      ValidationErrorResponse errorResponse =
          ValidationErrorResponse.builder()
              .validationErrors(
                  List.of(
                      ValidationError.builder()
                          .message("must not be null")
                          .fieldName("userType")
                          .build()))
              .build();

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody(ValidationErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("should throw 500 for RuntimeException")
    void unknown_error() {
      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("1006101000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.NON_DECLARING_TRADER)
              .originCountry("CN")
              .destinationCountry(UkCountry.GB)
              .build();

      when(commodityService.getCommodityMeasures(request))
          .thenReturn(Mono.error(new RuntimeException("Unknown error")));

      ErrorResponse errorResponse = ErrorResponse.builder().message("Unexpected error").build();

      webTestClient
          .get()
          .uri(createRequest(request))
          .exchange()
          .expectStatus()
          .is5xxServerError()
          .expectBody(ErrorResponse.class)
          .isEqualTo(errorResponse);
    }

    private Function<UriBuilder, URI> createRequest(CommodityMeasuresRequest request) {
      return builder ->
          Objects.nonNull(request.getImportDate())
              ? builder
                  .path(
                      String.format(
                          CONTEXT_ROOT + "/commodities/%s/measures", request.getCommodityCode()))
                  .queryParam("tradeType", request.getTradeType())
                  .queryParam("originCountry", request.getOriginCountry())
                  .queryParam("destinationCountry", request.getDestinationCountry())
                  .queryParam("userType", request.getUserType())
                  .queryParam(
                      "importDate",
                      String.format(
                          "%tY-%tm-%td",
                          request.getImportDate(),
                          request.getImportDate(),
                          request.getImportDate()))
                  .build()
              : builder
                  .path(
                      String.format(
                          CONTEXT_ROOT + "/commodities/%s/measures", request.getCommodityCode()))
                  .queryParam("tradeType", request.getTradeType())
                  .queryParam("originCountry", request.getOriginCountry())
                  .queryParam("destinationCountry", request.getDestinationCountry())
                  .queryParam("userType", request.getUserType())
                  .build();
    }

    private Function<UriBuilder, URI> createRequestWithUnknownTradeType(
        CommodityMeasuresRequest request) {
      return builder ->
          builder
              .path(
                  String.format(
                      CONTEXT_ROOT + "/commodities/%s/measures", request.getCommodityCode()))
              .queryParam("tradeType", "UNKNOWN")
              .queryParam("originCountry", request.getOriginCountry())
              .queryParam("destinationCountry", request.getDestinationCountry())
              .queryParam("userType", request.getUserType())
              .build();
    }

    private Function<UriBuilder, URI> createRequestWithUnknownUserType(
        CommodityMeasuresRequest request) {
      return builder ->
          builder
              .path(
                  String.format(
                      CONTEXT_ROOT + "/commodities/%s/measures", request.getCommodityCode()))
              .queryParam("userType", "UNKNOWN")
              .queryParam("tradeType", request.getTradeType())
              .queryParam("originCountry", request.getOriginCountry())
              .queryParam("destinationCountry", request.getDestinationCountry())
              .build();
    }

    private Function<UriBuilder, URI> createRequestWithUnrecognisedDestinationCountry(
        CommodityMeasuresRequest request) {
      return builder ->
          builder
              .path(
                  String.format(
                      CONTEXT_ROOT + "/commodities/%s/measures", request.getCommodityCode()))
              .queryParam("userType", request.getUserType())
              .queryParam("tradeType", request.getTradeType())
              .queryParam("originCountry", request.getOriginCountry())
              .queryParam("destinationCountry", "UNKNOWN")
              .build();
    }
  }
}
