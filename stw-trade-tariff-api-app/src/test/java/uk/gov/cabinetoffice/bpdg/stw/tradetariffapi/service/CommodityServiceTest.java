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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityChapter;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityGeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityHeading;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityImpl;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasure;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureType;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommoditySection;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.TradeTariffCommodityResponseIncludedEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityHierarchyItem;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityHierarchyType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityMeasures;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Header;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingHeaderContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingStepResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TaxAndDuty;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;

@ExtendWith(MockitoExtension.class)
public class CommodityServiceTest {

  private static final String COMMODITY_CODE = "0123456789";
  private static final String EXPECTED_MEASURE_TYPE_ID = "2";
  private static final String EXPECTED_MEASURE_TYPE_DESCRIPTION = "Measure Type 2";
  private static final String EXPECTED_MEASURE_TYPE_SERIES_ID = "B";
  private static final String GEOGRAPHICAL_AREA_ID = "1011";
  private static final Integer SECTION_ID = 1;
  private static final Integer CHAPTER_ID = 10;
  private static final Integer COMMODITY_HEADING_ID = 100;
  private static final List<TradeTariffCommodityResponseIncludedEntity> INCLUDED_ENTITIES =
    List.of(
      CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
      CommodityChapter.builder().id(String.valueOf(CHAPTER_ID)).build(),
      CommodityHeading.builder().id(String.valueOf(COMMODITY_HEADING_ID)).build(),
      CommodityMeasure.builder()
        .id("1")
        .isImport(true)
        .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
        .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
        .excludedCountries(Set.of("CN"))
        .measureConditionIds(Set.of("1", "2"))
        .build(),
      CommodityMeasureCondition.builder()
        .id("1")
        .conditionCode("B")
        .condition("B: Presentation of a certificate/licence/document")
        .documentCode("U088")
        .requirement(
          "Proofs of origin: Origin declaration stating European Union origin, in the context of the Canada-European Union Comprehensive Economic and Trade Agreement (CETA)")
        .action("Apply the mentioned duty")
        .dutyExpression("")
        .build(),
      CommodityMeasureCondition.builder()
        .id("2")
        .conditionCode("B")
        .condition("B: Presentation of a certificate/licence/document")
        .documentCode("")
        .requirement(null)
        .action("Measure not applicable")
        .dutyExpression("")
        .build(),
      CommodityMeasureCondition.builder()
        .id("1")
        .conditionCode("Y")
        .condition("Y: Other conditions")
        .documentCode("C678")
        .requirement(
          "Other certificates: Common Health Entry Document for Feed and Food of Non-Animal Origin (CHED-D) (as set out in Part 2, Section D of Annex II to Commission Implementing Regulation (EU) 2019/1715 (OJ L 261))")
        .action("Import/export allowed after control")
        .dutyExpression("")
        .build(),
      CommodityMeasureType.builder()
        .id("1")
        .description("Measure Type 1")
        .seriesId("O")
        .build(),
      CommodityMeasureType.builder()
        .id(EXPECTED_MEASURE_TYPE_ID)
        .description(EXPECTED_MEASURE_TYPE_DESCRIPTION)
        .seriesId(EXPECTED_MEASURE_TYPE_SERIES_ID)
        .build(),
      CommodityGeographicalArea.builder()
        .id(GEOGRAPHICAL_AREA_ID)
        .description("ERGA OMNES")
        .childrenGeographicalAreas(Set.of("CN"))
        .build(),
      CommodityGeographicalArea.builder().id("CN").description("China").build());
  @Mock private TradeTariffApiGateway tradeTariffApiGateway;
  @Mock private MeasureFilterer measureFilterer;
  @Mock private MeasureBuilder measureBuilder;
  @Mock private SignpostingContentService signpostingContentService;
  @Mock private ProhibitionContentService prohibitionContentService;
  @Mock private TaxApplicabilityService taxApplicabilityService;
  @InjectMocks private CommodityService commodityService;

  @BeforeEach
  public void setUp() {
    when(taxApplicabilityService.isApplicable(
      any(CommodityMeasuresRequest.class), any(TradeTariffCommodityResponse.class)))
      .thenReturn(Mono.just(false));
  }

  @Nested
  class GetCommodityMeasures {

    @Test
    @DisplayName("commodity data without attributes should return empty code and description")
    void commodityDataWithoutAttributes() {
      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
          .included(INCLUDED_ENTITIES)
          .build();
      List<Prohibition> prohibitions =
        List.of(
          Prohibition.builder()
            .measureTypeId("730")
            .legalAct("A1907950")
            .description("Prohibitions and restrictions enforced by customs on goods")
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      TradeType tradeType = TradeType.IMPORT;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .destinationCountry(destinationCountry)
          .tradeType(tradeType)
          .importDate(importDate)
          .commodityCode(COMMODITY_CODE)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        COMMODITY_CODE, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));
      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(List.of()));
      when(prohibitionContentService.getProhibitions(any(), any(), any(), eq(tradeType)))
        .thenReturn(Mono.just(prohibitions));
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityCode()).isEmpty();
      assertThat(commodityMeasuresResponse.getCommodityDescription()).isEmpty();
    }

    @Test
    @DisplayName("test with measure id of 481 - no prohibition")
    void measureWithId481() {
      final String EXPECTED_MEASURE_TYPE_ID_481 = "481";
      final String EXPECTED_MEASURE_TYPE_DESCRIPTION_481 = "Measure Type 481";
      final String EXPECTED_MEASURE_TYPE_SERIES_ID_481 = "481-ID";

      final List<TradeTariffCommodityResponseIncludedEntity> BASE_PROHIBITION_INCLUDED_ENTITIES =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder().id(String.valueOf(CHAPTER_ID)).build(),
          CommodityHeading.builder().id(String.valueOf(COMMODITY_HEADING_ID)).build(),
          CommodityMeasure.builder()
            .id("1")
            .isImport(true)
            .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
            .measureTypeId(EXPECTED_MEASURE_TYPE_ID_481)
            .excludedCountries(Set.of("CN"))
            .measureConditionIds(Set.of("1", "2"))
            .build(),
          CommodityMeasureCondition.builder()
            .id("1")
            .conditionCode("B")
            .condition("B: Presentation of a certificate/licence/document")
            .documentCode("U088")
            .requirement(
              "Proofs of origin: Origin declaration stating European Union origin, in the context of the Canada-European Union Comprehensive Economic and Trade Agreement (CETA)")
            .action("Apply the mentioned duty")
            .dutyExpression("")
            .build(),
          CommodityMeasureCondition.builder()
            .id("2")
            .conditionCode("B")
            .condition("B: Presentation of a certificate/licence/document")
            .documentCode("")
            .requirement(null)
            .action("Measure not applicable")
            .dutyExpression("")
            .build(),
          CommodityMeasureCondition.builder()
            .id("1")
            .conditionCode("Y")
            .condition("Y: Other conditions")
            .documentCode("C678")
            .requirement(
              "Other certificates: Common Health Entry Document for Feed and Food of Non-Animal Origin (CHED-D) (as set out in Part 2, Section D of Annex II to Commission Implementing Regulation (EU) 2019/1715 (OJ L 261))")
            .action("Import/export allowed after control")
            .dutyExpression("")
            .build(),
          CommodityMeasureType.builder()
            .id(EXPECTED_MEASURE_TYPE_ID_481)
            .description(EXPECTED_MEASURE_TYPE_DESCRIPTION_481)
            .seriesId(EXPECTED_MEASURE_TYPE_SERIES_ID_481)
            .build(),
          CommodityGeographicalArea.builder()
            .id(GEOGRAPHICAL_AREA_ID)
            .description("ERGA OMNES")
            .childrenGeographicalAreas(Set.of("CN"))
            .build(),
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityGeographicalArea.builder().id("CN").description("China").build());
      List<Prohibition> prohibitions = List.of();
      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(BASE_PROHIBITION_INCLUDED_ENTITIES)
          .build();

      List<SignpostingContent> signpostingStepResponses =
        List.of(
          SignpostingContent.builder()
            .headers(
              List.of(
                SignpostingHeaderContent.builder()
                  .header(
                    Header.builder()
                      .orderIndex(1)
                      .description("Get your business ready")
                      .build())
                  .steps(
                    List.of(
                      SignpostingStepResponse.builder()
                        .id(1)
                        .stepDescription("Get your commercial shipping documents")
                        .stepHowtoDescription(
                          "These are required for your goods to clear at the border")
                        .stepUrl(
                          "https://www.trade-tariff.service.gov.uk/sections")
                        .build()))
                  .build()))
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .commodityCode(COMMODITY_CODE)
          .tradeType(TradeType.IMPORT)
          .originCountry("CN")
          .importDate(importDate)
          .destinationCountry(destinationCountry)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        COMMODITY_CODE, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));
      when(measureFilterer.getRestrictiveMeasures(any(), any(), any()))
        .then(AdditionalAnswers.returnsFirstArg());
      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(signpostingStepResponses));
      when(prohibitionContentService.getProhibitions(any(), any(), any(), eq(commodityMeasuresRequest.getTradeType())))
        .thenReturn(Mono.just(prohibitions));
      // Call to service method
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      // Check the response is as expected
      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityCode()).isEqualTo(COMMODITY_CODE);
      assertThat(commodityMeasuresResponse.getCommodityDescription()).isEqualTo("description");
      assertThat(commodityMeasuresResponse.getProhibitions()).isEmpty();
    }

    @Test
    @DisplayName(
      "Test with measure series type A applicable to this country - this is a prohibition")
    void measureTypeWithMeasureSeriesTypeA() {
      final String PROHIBITED_EXPECTED_MEASURE_TYPE_ID = "277";
      final String PROHIBITED_EXPECTED_MEASURE_TYPE_DESCRIPTION = "Import prohibition";
      final String PROHIBITED_EXPECTED_MEASURE_TYPE_SERIES_ID = "A";

      final List<TradeTariffCommodityResponseIncludedEntity> BASE_PROHIBITION_INCLUDED_ENTITIES =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder().id(String.valueOf(CHAPTER_ID)).build(),
          CommodityHeading.builder().id(String.valueOf(COMMODITY_HEADING_ID)).build());
      val expectedProhibition =
        Prohibition.builder()
          .id("730")
          .legalAct("A1907950")
          .description("Prohibitions and restrictions enforced by customs on goods")
          .build();
      List<Prohibition> prohibitions = List.of(expectedProhibition);
      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(BASE_PROHIBITION_INCLUDED_ENTITIES)
          .build();
      List<SignpostingContent> signpostingStepResponses =
        List.of(
          SignpostingContent.builder()
            .headers(
              List.of(
                SignpostingHeaderContent.builder()
                  .header(
                    Header.builder()
                      .orderIndex(1)
                      .description("Get your business ready")
                      .build())
                  .steps(
                    List.of(
                      SignpostingStepResponse.builder()
                        .id(1)
                        .stepDescription("Get your commercial shipping documents")
                        .stepHowtoDescription(
                          "These are required for your goods to clear at the border")
                        .stepUrl(
                          "https://www.trade-tariff.service.gov.uk/sections")
                        .build()))
                  .build()))
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .commodityCode(COMMODITY_CODE)
          .tradeType(TradeType.IMPORT)
          .originCountry("CN")
          .importDate(importDate)
          .destinationCountry(destinationCountry)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        COMMODITY_CODE, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));
      when(measureBuilder.from(tradeTariffCommodityResponse))
        .thenReturn(
          List.of(
            Measure.builder()
              .measureType(
                MeasureType.builder()
                  .id(PROHIBITED_EXPECTED_MEASURE_TYPE_ID)
                  .description(PROHIBITED_EXPECTED_MEASURE_TYPE_DESCRIPTION)
                  .seriesId(PROHIBITED_EXPECTED_MEASURE_TYPE_SERIES_ID)
                  .build())
              .geographicalArea(
                GeographicalArea.builder()
                  .id(GEOGRAPHICAL_AREA_ID)
                  .description("ERGA OMNES")
                  .childrenGeographicalAreas(Set.of("CN"))
                  .build())
              .build()));
      when(measureFilterer.getRestrictiveMeasures(any(), any(), any()))
        .then(AdditionalAnswers.returnsFirstArg());
      when(measureFilterer.maybeFilterByAdditionalCode(any(), any()))
        .then(AdditionalAnswers.returnsFirstArg());
      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(signpostingStepResponses));
      when(prohibitionContentService.getProhibitions(any(), any(), any(), eq(commodityMeasuresRequest.getTradeType())))
        .thenReturn(Mono.just(prohibitions));
      // Call to service method
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();
      // Check the response is as expected
      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityCode()).isEqualTo(COMMODITY_CODE);
      assertThat(commodityMeasuresResponse.getCommodityDescription()).isEqualTo("description");
      assertThat(commodityMeasuresResponse.getMeasures()).hasSize(1);
      Measure measure = commodityMeasuresResponse.getMeasures().get(0);
      assertThat(measure.getGeographicalArea())
        .isEqualTo(
          GeographicalArea.builder()
            .id(GEOGRAPHICAL_AREA_ID)
            .description("ERGA OMNES")
            .childrenGeographicalAreas(Set.of("CN"))
            .build());
      assertThat(measure.getMeasureType())
        .isEqualTo(
          MeasureType.builder()
            .id(PROHIBITED_EXPECTED_MEASURE_TYPE_ID)
            .description(PROHIBITED_EXPECTED_MEASURE_TYPE_DESCRIPTION)
            .seriesId(PROHIBITED_EXPECTED_MEASURE_TYPE_SERIES_ID)
            .build());
      assertThat(commodityMeasuresResponse.getProhibitions()).isNotEmpty();
      assertThat(commodityMeasuresResponse.getProhibitions()).hasSize(1);
      assertThat(commodityMeasuresResponse.getProhibitions().get(0)).isEqualTo(expectedProhibition);
    }

    @Test
    @DisplayName("Test with measure series type B and no condition codes - prohibition")
    void measureTypeWithMeasureSeriesTypeBNoConditions() {
      List<TradeTariffCommodityResponseIncludedEntity> BASE_PROHIBITION_INCLUDED_ENTITIES =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder().id(String.valueOf(CHAPTER_ID)).build(),
          CommodityHeading.builder().id(String.valueOf(COMMODITY_HEADING_ID)).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(BASE_PROHIBITION_INCLUDED_ENTITIES)
          .build();
      List<SignpostingContent> signpostingStepResponses =
        List.of(
          SignpostingContent.builder()
            .headers(
              List.of(
                SignpostingHeaderContent.builder()
                  .header(
                    Header.builder()
                      .orderIndex(1)
                      .description("Get your business ready")
                      .build())
                  .steps(
                    List.of(
                      SignpostingStepResponse.builder()
                        .id(1)
                        .stepDescription("Get your commercial shipping documents")
                        .stepHowtoDescription(
                          "These are required for your goods to clear at the border")
                        .stepUrl(
                          "https://www.trade-tariff.service.gov.uk/sections")
                        .build()))
                  .build()))
            .build());
      List<Prohibition> prohibitions =
        List.of(
          Prohibition.builder()
            .id("730")
            .legalAct("A1907950")
            .description("Prohibitions and restrictions enforced by customs on goods")
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      TradeType tradeType = TradeType.IMPORT;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .commodityCode(COMMODITY_CODE)
          .tradeType(tradeType)
          .originCountry("CN")
          .importDate(importDate)
          .destinationCountry(destinationCountry)
          .build();

      List<Measure> measuresList = List.of(
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id(EXPECTED_MEASURE_TYPE_ID)
                      .description(EXPECTED_MEASURE_TYPE_DESCRIPTION)
                      .seriesId(EXPECTED_MEASURE_TYPE_SERIES_ID)
                      .build())
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .childrenGeographicalAreas(Set.of("CN"))
                      .build())
              .build());

      when(tradeTariffApiGateway.getCommodity(COMMODITY_CODE, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));
      when(measureBuilder.from(tradeTariffCommodityResponse))
        .thenReturn(measuresList);
      when(measureFilterer.getRestrictiveMeasures(measuresList, tradeType, "CN"))
        .thenReturn(measuresList);
      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(signpostingStepResponses));
      when(prohibitionContentService.getProhibitions(any(), any(), any(), eq(commodityMeasuresRequest.getTradeType())))
        .thenReturn(Mono.just(prohibitions));
      // Call to service method
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      // Check the response is as expected
      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityCode()).isEqualTo(COMMODITY_CODE);
      assertThat(commodityMeasuresResponse.getCommodityDescription()).isEqualTo("description");
      assertThat(commodityMeasuresResponse.getProhibitions()).hasSize(1);
    }

    @Test
    @DisplayName("Commodity measures when it has additional codes and no prohibitions")
    void commodityMeasuresWhenItHasAdditionalCodesPlusNoProhibitions() throws Exception {
      // given
      var commodityCode = "9706000000";
      var objectMapper = new ObjectMapper();
      var commodityResponseFilePath = "src/test/resources/stubs/commodity_with_prohibitions.json";
      var tradeTariffCommodityResponse =
        objectMapper.readValue(
          new File(commodityResponseFilePath), TradeTariffCommodityResponse.class);
      List<SignpostingContent> signpostingStepResponses =
        List.of(
          SignpostingContent.builder()
            .headers(
              List.of(
                SignpostingHeaderContent.builder()
                  .header(
                    Header.builder()
                      .orderIndex(1)
                      .description("Get your business ready")
                      .build())
                  .steps(
                    List.of(
                      SignpostingStepResponse.builder()
                        .id(1)
                        .stepDescription("Get your commercial shipping documents")
                        .stepHowtoDescription(
                          "These are required for your goods to clear at the border")
                        .stepUrl(
                          "https://www.trade-tariff.service.gov.uk/sections")
                        .build()))
                  .build()))
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .commodityCode(commodityCode)
          .tradeType(TradeType.IMPORT)
          .originCountry("IQ")
          .importDate(importDate)
          .destinationCountry(destinationCountry)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        commodityCode, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));

      var measures =
        List.of(
          Measure.builder()
            .measureType(
              MeasureType.builder()
                .id("1")
                .description("Measure Type-1")
                .seriesId("B")
                .build())
            .geographicalArea(
              GeographicalArea.builder()
                .description("GeographicalArea-1")
                .childrenGeographicalAreas(Set.of("IQ"))
                .build())
            .additionalCode(
              AdditionalCode.builder()
                .code("4010")
                .description(
                  "Archaeological objects more than 100 years old which are products of excavations and finds on the land or under water, archaeological sites or archaeological collections")
                .build())
            .build(),
          Measure.builder()
            .measureType(
              MeasureType.builder()
                .id("2")
                .description("Measure Type-2")
                .seriesId("A")
                .build())
            .geographicalArea(
              GeographicalArea.builder()
                .description("GeographicalArea-2")
                .childrenGeographicalAreas(Set.of("IQ"))
                .build())
            .additionalCode(
              AdditionalCode.builder()
                .code("4099")
                .description(
                  "Other than those mentioned in Regulation (EC) no 1210/2003 (OJ L 169): no restrictions")
                .build())
            .build());

      when(measureBuilder.from(tradeTariffCommodityResponse))
        .thenReturn(
          List.of(
            Measure.builder()
              .measureType(
                MeasureType.builder()
                  .id("1")
                  .description("Measure Type-1")
                  .seriesId("B")
                  .build())
              .geographicalArea(
                GeographicalArea.builder()
                  .description("GeographicalArea-1")
                  .childrenGeographicalAreas(Set.of("IQ"))
                  .build())
              .additionalCode(
                AdditionalCode.builder()
                  .code("4010")
                  .description(
                    "Archaeological objects more than 100 years old which are products of excavations and finds on the land or under water, archaeological sites or archaeological collections")
                  .build())
              .build(),
            Measure.builder()
              .measureType(
                MeasureType.builder()
                  .id("2")
                  .description("Measure Type-2")
                  .seriesId("A")
                  .build())
              .geographicalArea(
                GeographicalArea.builder()
                  .description("GeographicalArea-2")
                  .childrenGeographicalAreas(Set.of("IQ"))
                  .build())
              .additionalCode(
                AdditionalCode.builder()
                  .code("4099")
                  .description(
                    "Other than those mentioned in Regulation (EC) no 1210/2003 (OJ L 169): no restrictions")
                  .build())
              .build()));

      var additionalCodeFilteredMeasures =
        measures.stream()
          .filter(
            measure -> {
              var additionCode =
                measure.getAdditionalCode().isPresent()
                  ? measure.getAdditionalCode().get().getCode()
                  : "";
              return additionCode.equalsIgnoreCase("4099");
            })
          .collect(Collectors.toList());

      when(measureFilterer.getRestrictiveMeasures(any(), any(), any()))
        .then(AdditionalAnswers.returnsFirstArg());
      when(measureFilterer.maybeFilterByAdditionalCode(any(), any()))
        .thenReturn(additionalCodeFilteredMeasures);

      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(signpostingStepResponses));
      when(prohibitionContentService.getProhibitions(
        additionalCodeFilteredMeasures, "IQ", Locale.EN, commodityMeasuresRequest.getTradeType()))
        .thenReturn(Mono.just(List.of()));
      // Call to service method
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      // Check the response is as expected
      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityCode()).isEqualTo(commodityCode);
      assertThat(commodityMeasuresResponse.getCommodityDescription())
        .contains("Antiques of an age exceeding");
      assertThat(commodityMeasuresResponse.getProhibitions()).isEmpty();
    }

    @Test
    @DisplayName("Commodity measures when it has additional codes and prohibitions")
    void commodityMeasuresWhenItHasAdditionalCodesPlusProhibitions() throws Exception {
      // given
      var commodityCode = "9706000000";
      var objectMapper = new ObjectMapper();
      var commodityResponseFilePath = "src/test/resources/stubs/commodity_with_prohibitions.json";
      var tradeTariffCommodityResponse =
        objectMapper.readValue(
          new File(commodityResponseFilePath), TradeTariffCommodityResponse.class);
      List<SignpostingContent> signpostingStepResponses =
        List.of(
          SignpostingContent.builder()
            .headers(
              List.of(
                SignpostingHeaderContent.builder()
                  .header(
                    Header.builder()
                      .orderIndex(1)
                      .description("Get your business ready")
                      .build())
                  .steps(
                    List.of(
                      SignpostingStepResponse.builder()
                        .id(1)
                        .stepDescription("Get your commercial shipping documents")
                        .stepHowtoDescription(
                          "These are required for your goods to clear at the border")
                        .stepUrl(
                          "https://www.trade-tariff.service.gov.uk/sections")
                        .build()))
                  .build()))
            .build());
      List<Prohibition> prohibitions =
        List.of(
          Prohibition.builder()
            .id("465")
            .legalAct("X2007070")
            .description(
              "## There are restrictions on the import of these goods from Iraq%0A%0AThere are [restrictions on the import of cultural goods from Iraq](https://www.gov.uk/government/collections/uk-sanctions-on-iraq)%0A%0AYou must ensure that your goods were not illegally removed from Iraq since 6 August 1990.%0A%0ARead [further information on the import of cultural goods](https://www.gov.uk/guidance/exporting-or-importing-objects-of-cultural-interest)")
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .commodityCode(commodityCode)
          .tradeType(TradeType.IMPORT)
          .originCountry("IQ")
          .importDate(importDate)
          .destinationCountry(destinationCountry)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        commodityCode, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));

      var measures =
        List.of(
          Measure.builder()
            .id("20088568")
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .measureType(
              MeasureType.builder()
                .id("1")
                .description("Measure Type-1")
                .seriesId("B")
                .build())
            .geographicalArea(
              GeographicalArea.builder()
                .description("GeographicalArea-1")
                .childrenGeographicalAreas(Set.of("IQ"))
                .build())
            .additionalCode(
              AdditionalCode.builder()
                .code("4010")
                .description(
                  "Archaeological objects more than 100 years old which are products of excavations and finds on the land or under water, archaeological sites or archaeological collections")
                .build())
            .build(),
          Measure.builder()
            .id("20134109")
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .measureType(
              MeasureType.builder()
                .id("2")
                .description("Measure Type-2")
                .seriesId("A")
                .build())
            .geographicalArea(
              GeographicalArea.builder()
                .description("GeographicalArea-2")
                .childrenGeographicalAreas(Set.of("IQ"))
                .build())
            .additionalCode(
              AdditionalCode.builder()
                .code("4099")
                .description(
                  "Other than those mentioned in Regulation (EC) no 1210/2003 (OJ L 169): no restrictions")
                .build())
            .build());

      when(measureBuilder.from(tradeTariffCommodityResponse))
        .thenReturn(
          List.of(
            Measure.builder()
              .id("20088568")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                MeasureType.builder()
                  .id("1")
                  .description("Measure Type-1")
                  .seriesId("B")
                  .build())
              .geographicalArea(
                GeographicalArea.builder()
                  .description("GeographicalArea-1")
                  .childrenGeographicalAreas(Set.of("IQ"))
                  .build())
              .additionalCode(
                AdditionalCode.builder()
                  .code("4010")
                  .description(
                    "Archaeological objects more than 100 years old which are products of excavations and finds on the land or under water, archaeological sites or archaeological collections")
                  .build())
              .build(),
            Measure.builder()
              .id("20134109")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                MeasureType.builder()
                  .id("2")
                  .description("Measure Type-2")
                  .seriesId("A")
                  .build())
              .geographicalArea(
                GeographicalArea.builder()
                  .description("GeographicalArea-2")
                  .childrenGeographicalAreas(Set.of("IQ"))
                  .build())
              .additionalCode(
                AdditionalCode.builder()
                  .code("4099")
                  .description(
                    "Other than those mentioned in Regulation (EC) no 1210/2003 (OJ L 169): no restrictions")
                  .build())
              .build()));

      var additionalCodeFilteredMeasures =
        measures.stream()
          .filter(
            measure -> {
              var additionCode =
                measure.getAdditionalCode().isPresent()
                  ? measure.getAdditionalCode().get().getCode()
                  : "";
              return additionCode.equalsIgnoreCase("4010");
            })
          .collect(Collectors.toList());

      when(measureFilterer.getRestrictiveMeasures(any(), any(), any()))
        .then(AdditionalAnswers.returnsFirstArg());
      when(measureFilterer.maybeFilterByAdditionalCode(any(), any()))
        .thenReturn(additionalCodeFilteredMeasures);

      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(signpostingStepResponses));
      when(prohibitionContentService.getProhibitions(
        additionalCodeFilteredMeasures, "IQ", Locale.EN, commodityMeasuresRequest.getTradeType()))
        .thenReturn(Mono.just(prohibitions));
      // Call to service method
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      // Check the response is as expected
      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityCode()).isEqualTo(commodityCode);
      assertThat(commodityMeasuresResponse.getCommodityDescription())
        .contains("Antiques of an age exceeding");
      assertThat(commodityMeasuresResponse.getProhibitions()).hasSize(1);
    }

    @Test
    @DisplayName("should return commodity hierarchy from trade tariff api response")
    void shouldReturnCommodityHierarchyFromTradeTariffApiResponse() {
      String firstAncestorCommodityCode = "0103910000";
      String secondAncestorCommodityCode = "0103920000";
      String thirdAncestorCommodityCode = "0103921100";
      String chapterNomenclatureId = "0100000000";
      String headingNomenclatureId = "0103000000";
      final List<TradeTariffCommodityResponseIncludedEntity> INCLUDED_ENTITIES =
        List.of(
          CommoditySection.builder()
            .id(String.valueOf(SECTION_ID))
            .description("Live animals; animal products")
            .build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(chapterNomenclatureId)
            .description("Live animals")
            .build(),
          CommodityHeading.builder()
            .id(String.valueOf(COMMODITY_HEADING_ID))
            .goodsNomenclatureItemId(headingNomenclatureId)
            .description("Live swine")
            .build(),
          CommodityImpl.builder()
            .goodsNomenclatureItemId(firstAncestorCommodityCode)
            .description("Other")
            .build(),
          CommodityImpl.builder()
            .goodsNomenclatureItemId(secondAncestorCommodityCode)
            .description("Weighing 50 kg or more")
            .build(),
          CommodityImpl.builder()
            .goodsNomenclatureItemId(thirdAncestorCommodityCode)
            .description("Domestic species")
            .build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(INCLUDED_ENTITIES)
          .build();
      List<Prohibition> prohibitions =
        List.of(
          Prohibition.builder()
            .id("730")
            .legalAct("A1907950")
            .description("Prohibitions and restrictions enforced by customs on goods")
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .commodityCode(COMMODITY_CODE)
          .tradeType(TradeType.IMPORT)
          .originCountry("CN")
          .importDate(importDate)
          .destinationCountry(destinationCountry)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        COMMODITY_CODE, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));
      when(measureFilterer.getRestrictiveMeasures(any(), any(), any()))
        .then(AdditionalAnswers.returnsFirstArg());
      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(List.of()));
      when(prohibitionContentService.getProhibitions(any(), any(), any(), eq(commodityMeasuresRequest.getTradeType())))
        .thenReturn(Mono.just(prohibitions));
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityHierarchy())
        .containsExactly(
          new CommodityHierarchyItem(
            String.valueOf(SECTION_ID),
            "Live animals; animal products",
            CommodityHierarchyType.SECTION),
          new CommodityHierarchyItem(
            chapterNomenclatureId, "Live animals", CommodityHierarchyType.CHAPTER),
          new CommodityHierarchyItem(
            headingNomenclatureId, "Live swine", CommodityHierarchyType.HEADING),
          new CommodityHierarchyItem(
            firstAncestorCommodityCode, "Other", CommodityHierarchyType.COMMODITY),
          new CommodityHierarchyItem(
            secondAncestorCommodityCode,
            "Weighing 50 kg or more",
            CommodityHierarchyType.COMMODITY),
          new CommodityHierarchyItem(
            thirdAncestorCommodityCode,
            "Domestic species",
            CommodityHierarchyType.COMMODITY));
    }

    @Test
    @DisplayName("should return hierarchy for commodity which is also a heading")
    void shouldReturnCommodityHierarchyFromCommodityWhichIsAlsoAHeading() {
      String chapterNomenclatureId = "0100000000";
      final List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
        List.of(
          CommoditySection.builder()
            .id(String.valueOf(SECTION_ID))
            .description("Live animals; animal products")
            .build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(chapterNomenclatureId)
            .description("Live animals")
            .build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("heading")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(includedEntities)
          .build();
      List<Prohibition> prohibitions =
        List.of(
          Prohibition.builder()
            .id("730")
            .legalAct("A1907950")
            .description("Prohibitions and restrictions enforced by customs on goods")
            .build());
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .commodityCode(COMMODITY_CODE)
          .tradeType(TradeType.IMPORT)
          .originCountry("CN")
          .importDate(importDate)
          .destinationCountry(destinationCountry)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        COMMODITY_CODE, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));
      when(measureFilterer.getRestrictiveMeasures(any(), any(), any()))
        .then(AdditionalAnswers.returnsFirstArg());
      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(List.of()));
      when(prohibitionContentService.getProhibitions(any(), any(), any(), eq(commodityMeasuresRequest.getTradeType())))
        .thenReturn(Mono.just(prohibitions));
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getCommodityHierarchy())
        .containsExactly(
          new CommodityHierarchyItem(
            String.valueOf(SECTION_ID),
            "Live animals; animal products",
            CommodityHierarchyType.SECTION),
          new CommodityHierarchyItem(
            chapterNomenclatureId, "Live animals", CommodityHierarchyType.CHAPTER));
    }

    @Test
    @DisplayName("should return tax and duty information")
    void shouldReturnTaxAndDuty() {
      // given
      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
          .included(INCLUDED_ENTITIES)
          .build();
      var importDate = LocalDate.now();
      UkCountry destinationCountry = UkCountry.GB;
      CommodityMeasuresRequest commodityMeasuresRequest =
        CommodityMeasuresRequest.builder()
          .destinationCountry(destinationCountry)
          .importDate(importDate)
          .commodityCode(COMMODITY_CODE)
          .build();

      when(tradeTariffApiGateway.getCommodity(
        COMMODITY_CODE, importDate, destinationCountry))
        .thenReturn(Mono.just(tradeTariffCommodityResponse));
      when(signpostingContentService.getSignpostingContents(
        eq(commodityMeasuresRequest), eq(tradeTariffCommodityResponse), anyList()))
        .thenReturn(Mono.just(List.of()));
      when(prohibitionContentService.getProhibitions(any(), any(), any(), eq(commodityMeasuresRequest.getTradeType())))
        .thenReturn(Mono.just(Collections.emptyList()));
      when(taxApplicabilityService.isApplicable(
        commodityMeasuresRequest, tradeTariffCommodityResponse))
        .thenReturn(Mono.just(true));

      // when
      CommodityMeasures commodityMeasuresResponse =
        commodityService.getCommodityMeasures(commodityMeasuresRequest).block();

      // then
      assertThat(commodityMeasuresResponse).isNotNull();
      assertThat(commodityMeasuresResponse.getTaxAndDuty()).isEqualTo(TaxAndDuty.builder().applicable(true).build());
    }
  }
}
