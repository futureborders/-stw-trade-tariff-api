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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityChapter;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityGeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityHeading;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasure;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureType;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommoditySection;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.TradeTariffCommodityResponseIncludedEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ConditionBasedRestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.MeasuresRequest;

@ExtendWith(MockitoExtension.class)
class MeasuresServiceTest {

  private static final String GEOGRAPHICAL_AREA_ID = "1011";
  private static final Integer SECTION_ID = 1;
  private static final Integer CHAPTER_ID = 10;
  private static final Integer COMMODITY_HEADING_ID = 100;
  private static final String MEASURE_TYPE_ID = "2";
  private static final String MEASURE_TYPE_DESCRIPTION = "Measure Type 2";
  private static final String MEASURE_TYPE_SERIES_ID = "B";

  @Mock private TradeTariffApiGateway tradeTariffApiGateway;
  @Mock private MeasureFilterer measureFilterer;
  @Mock private MeasureBuilder measureBuilder;
  @Mock private MeasureTypeService measureTypeService;
  @Mock private ProhibitionContentService prohibitionContentService;

  @InjectMocks
  private MeasuresService measuresService;

  @Test
  public void shouldReturnMeasuresWhenAdditionalCodeIsNotProvidedForImports() {
    String commodityCode = "1234567890";
    LocalDate importDate = LocalDate.now();
    UkCountry destinationCountry = UkCountry.GB;
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

    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .included(includedEntities())
        .build();

    when(tradeTariffApiGateway.getCommodity(commodityCode, importDate, destinationCountry))
      .thenReturn(Mono.just(tradeTariffCommodityResponse));
    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure1 = ConditionBasedRestrictiveMeasure.builder().build();
    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure2 = ConditionBasedRestrictiveMeasure.builder().build();
    Prohibition prohibition = Prohibition.builder().build();
    List<Measure> measuresList = mock(List.class);
    List<Prohibition> prohibitions = List.of(prohibition);
    List<ConditionBasedRestrictiveMeasure> conditionBasedRestrictiveMeasureList =
      List.of(conditionBasedRestrictiveMeasure1, conditionBasedRestrictiveMeasure2);
    when(measureBuilder.from(tradeTariffCommodityResponse)).thenReturn(measuresList);
    when(measureFilterer.getRestrictiveMeasures(measuresList, tradeType, originCountry))
      .thenReturn(measuresList);
    when(prohibitionContentService.getProhibitions(measuresList, originCountry, Locale.EN, tradeType)).thenReturn(Mono.just(prohibitions));
    when(measureFilterer.maybeFilterByAdditionalCode(measuresList, Optional.empty())).thenReturn(measuresList);
    when(measureTypeService.getSignpostingMeasureTypeContents(
      measuresList, commodityCode, destinationCountry))
      .thenReturn(Flux.fromIterable(conditionBasedRestrictiveMeasureList));

    StepVerifier.create(measuresService.getMeasures(request))
      .expectNext(conditionBasedRestrictiveMeasure1)
      .expectNext(conditionBasedRestrictiveMeasure2)
      .expectNext(prohibition)
      .verifyComplete();
  }

  @Test
  public void shouldReturnMeasuresWhenAdditionalCodeIsNotProvidedForExports() {
    String commodityCode = "1234567890";
    LocalDate importDate = LocalDate.now();
    String destinationCountry = "CN";
    TradeType tradeType = TradeType.EXPORT;
    UkCountry originCountry =  UkCountry.GB;

    MeasuresRequest request =
      MeasuresRequest.builder()
        .tradeType(tradeType)
        .dateOfTrade(importDate)
        .originCountry(originCountry.name())
        .destinationCountry(destinationCountry)
        .commodityCode(commodityCode)
        .build();

    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .included(includedEntities())
        .build();

    when(tradeTariffApiGateway.getCommodity(commodityCode, importDate, originCountry))
      .thenReturn(Mono.just(tradeTariffCommodityResponse));
    List<Measure> measuresList = mock(List.class);

    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure1 = ConditionBasedRestrictiveMeasure.builder().build();
    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure2 = ConditionBasedRestrictiveMeasure.builder().build();
    Prohibition prohibition = Prohibition.builder().build();
    List<Prohibition> prohibitions = List.of(prohibition);

    List<ConditionBasedRestrictiveMeasure> conditionBasedRestrictiveMeasureList =
      List.of(conditionBasedRestrictiveMeasure1, conditionBasedRestrictiveMeasure2);
    when(measureBuilder.from(tradeTariffCommodityResponse)).thenReturn(measuresList);
    when(measureFilterer.getRestrictiveMeasures(measuresList, tradeType, destinationCountry))
      .thenReturn(measuresList);
    when(measureFilterer.maybeFilterByAdditionalCode(measuresList, Optional.empty())).thenReturn(measuresList);
    when(measureTypeService.getSignpostingMeasureTypeContents(
      measuresList, commodityCode, originCountry))
      .thenReturn(Flux.fromIterable(conditionBasedRestrictiveMeasureList));
    when(prohibitionContentService.getProhibitions(measuresList, destinationCountry, Locale.EN, tradeType)).thenReturn(Mono.just(prohibitions));

    StepVerifier.create(measuresService.getMeasures(request))
      .expectNext(conditionBasedRestrictiveMeasure1)
      .expectNext(conditionBasedRestrictiveMeasure2)
      .expectNext(prohibition)
      .verifyComplete();
  }

  @Test
  public void shouldReturnMeasuresWhenAdditionalCodeIsProvidedForImports() {
    String commodityCode = "1234567890";
    LocalDate importDate = LocalDate.now();
    UkCountry destinationCountry = UkCountry.GB;
    TradeType tradeType = TradeType.IMPORT;
    String originCountry = "CN";
    String additionalCode = "1234";

    MeasuresRequest request =
      MeasuresRequest.builder()
        .tradeType(tradeType)
        .dateOfTrade(importDate)
        .originCountry(originCountry)
        .destinationCountry(destinationCountry.name())
        .commodityCode(commodityCode)
        .additionalCode(additionalCode)
        .build();

    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .included(includedEntities())
        .build();

    when(tradeTariffApiGateway.getCommodity(commodityCode, importDate, destinationCountry))
      .thenReturn(Mono.just(tradeTariffCommodityResponse));

    List<Measure> measuresList = mock(List.class);
    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure1 = ConditionBasedRestrictiveMeasure.builder().build();
    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure2 = ConditionBasedRestrictiveMeasure.builder().build();
    Prohibition prohibition = Prohibition.builder().build();
    List<Prohibition> prohibitions = List.of(prohibition);

    List<ConditionBasedRestrictiveMeasure> conditionBasedRestrictiveMeasureList =
      List.of(conditionBasedRestrictiveMeasure1, conditionBasedRestrictiveMeasure2);
    when(measureBuilder.from(tradeTariffCommodityResponse)).thenReturn(measuresList);
    when(measureFilterer.getRestrictiveMeasures(measuresList, tradeType, originCountry))
      .thenReturn(measuresList);
    when(measureFilterer.maybeFilterByAdditionalCode(measuresList, Optional.of(additionalCode))).thenReturn(measuresList);
    when(measureTypeService.getSignpostingMeasureTypeContents(
      measuresList, commodityCode, destinationCountry))
      .thenReturn(Flux.fromIterable(conditionBasedRestrictiveMeasureList));
    when(prohibitionContentService.getProhibitions(measuresList, originCountry, Locale.EN, tradeType)).thenReturn(Mono.just(prohibitions));

    StepVerifier.create(measuresService.getMeasures(request))
      .expectNext(conditionBasedRestrictiveMeasure1)
      .expectNext(conditionBasedRestrictiveMeasure2)
      .expectNext(prohibition)
      .verifyComplete();
  }

  @Test
  public void shouldReturnMeasuresWhenAdditionalCodeIsProvidedForExports() {
    String commodityCode = "1234567890";
    LocalDate importDate = LocalDate.now();
    UkCountry originCountry = UkCountry.GB;
    TradeType tradeType = TradeType.EXPORT;
    String destinationCountry = "CN";
    String additionalCode = "1234";

    MeasuresRequest request =
      MeasuresRequest.builder()
        .tradeType(tradeType)
        .dateOfTrade(importDate)
        .originCountry(originCountry.name())
        .destinationCountry(destinationCountry)
        .commodityCode(commodityCode)
        .additionalCode(additionalCode)
        .build();

    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .included(includedEntities())
        .build();

    when(tradeTariffApiGateway.getCommodity(commodityCode, importDate, originCountry))
      .thenReturn(Mono.just(tradeTariffCommodityResponse));

    List<Measure> measuresList = mock(List.class);
    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure1 = ConditionBasedRestrictiveMeasure.builder().build();
    ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure2 = ConditionBasedRestrictiveMeasure.builder().build();
    Prohibition prohibition = Prohibition.builder().build();
    List<Prohibition> prohibitions = List.of(prohibition);

    List<ConditionBasedRestrictiveMeasure> conditionBasedRestrictiveMeasureList =
      List.of(conditionBasedRestrictiveMeasure1, conditionBasedRestrictiveMeasure2);
    when(measureBuilder.from(tradeTariffCommodityResponse)).thenReturn(measuresList);
    when(measureFilterer.getRestrictiveMeasures(measuresList, tradeType, destinationCountry))
      .thenReturn(measuresList);
    when(measureFilterer.maybeFilterByAdditionalCode(measuresList, Optional.of(additionalCode))).thenReturn(measuresList);
    when(measureTypeService.getSignpostingMeasureTypeContents(
      measuresList, commodityCode, originCountry))
      .thenReturn(Flux.fromIterable(conditionBasedRestrictiveMeasureList));
    when(prohibitionContentService.getProhibitions(measuresList, destinationCountry, Locale.EN, tradeType)).thenReturn(Mono.just(prohibitions));

    StepVerifier.create(measuresService.getMeasures(request))
      .expectNext(conditionBasedRestrictiveMeasure1)
      .expectNext(conditionBasedRestrictiveMeasure2)
      .expectNext(prohibition)
      .verifyComplete();
  }

  private List<TradeTariffCommodityResponseIncludedEntity> includedEntities() {
    return List.of(
      CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
      CommodityChapter.builder().id(String.valueOf(CHAPTER_ID)).build(),
      CommodityHeading.builder().id(String.valueOf(COMMODITY_HEADING_ID)).build(),
      CommodityMeasure.builder()
        .id("1")
        .isImport(true)
        .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
        .measureTypeId(MEASURE_TYPE_ID)
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
      CommodityMeasureType.builder().id("1").description("Measure Type 1").seriesId("O").build(),
      CommodityMeasureType.builder()
        .id(MEASURE_TYPE_ID)
        .description(MEASURE_TYPE_DESCRIPTION)
        .seriesId(MEASURE_TYPE_SERIES_ID)
        .build(),
      CommodityGeographicalArea.builder()
        .id(GEOGRAPHICAL_AREA_ID)
        .description("ERGA OMNES")
        .childrenGeographicalAreas(Set.of("CN"))
        .build(),
      CommodityGeographicalArea.builder().id("CN").description("China").build());
  }
}
