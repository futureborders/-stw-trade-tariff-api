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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData.DutyCalculatorAdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityAdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityChapter;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityGeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityHeading;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasure;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureType;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommoditySection;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.DutyExpression;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.TradeTariffCommodityResponseIncludedEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@ExtendWith(MockitoExtension.class)
class MeasureBuilderTest {

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
        .additionalCodeId("11529")
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
      CommodityGeographicalArea.builder().id("CN").description("China").build(),
      CommodityAdditionalCode.builder()
        .id("11529")
        .code("4200")
        .description("Procyon lotor")
        .build());

  private MeasureBuilder measureBuilder;

  @BeforeEach
  void setUp() {
    measureBuilder = new MeasureBuilder();
  }

  @Test
  void shouldBuildMeasures() {
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

    List<Measure> measures = measureBuilder.from(tradeTariffCommodityResponse);

    assertThat(measures).hasSize(1);

    Measure measure = measures.get(0);
    assertThat(measure.getId()).isEqualTo("1");
    assertThat(measure.getApplicableTradeTypes()).isEqualTo(List.of(TradeType.IMPORT));
    assertThat(measure.getExcludedCountries()).containsExactly("CN");
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
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description(EXPECTED_MEASURE_TYPE_DESCRIPTION)
          .seriesId(EXPECTED_MEASURE_TYPE_SERIES_ID)
          .build());
    assertThat(measure.getMeasureConditions())
      .containsExactlyInAnyOrder(
        MeasureCondition.builder()
          .id("1")
          .conditionCode(MeasureConditionCode.B)
          .condition("B: Presentation of a certificate/licence/document")
          .documentCode("U088")
          .requirement(
            "Proofs of origin: Origin declaration stating European Union origin, in the context of the Canada-European Union Comprehensive Economic and Trade Agreement (CETA)")
          .action("Apply the mentioned duty")
          .dutyExpression("")
          .build(),
        MeasureCondition.builder()
          .id("2")
          .conditionCode(MeasureConditionCode.B)
          .condition("B: Presentation of a certificate/licence/document")
          .documentCode("")
          .requirement(null)
          .action("Measure not applicable")
          .dutyExpression("")
          .build());
    assertThat(measure.getAdditionalCode())
      .isPresent()
      .get()
      .isEqualTo(AdditionalCode.builder().code("4200").description("Procyon lotor").build());
  }

  @Test
  @DisplayName("measures without measure type should fail")
  void measureWithoutMeasureType() {
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
          .build());
    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .included(includedEntities)
        .build();

    assertThatIllegalArgumentException()
      .isThrownBy(() -> measureBuilder.from(tradeTariffCommodityResponse))
      .withMessage("Cannot find measure type for measure 1");
  }

  @Test
  @DisplayName("measures without geographical area should fail")
  void measureWithoutGeographicalArea() {

    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build());
    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .included(includedEntities)
        .build();

    assertThatIllegalArgumentException()
      .isThrownBy(() -> measureBuilder.from(tradeTariffCommodityResponse))
      .withMessage("Cannot find geographical area for measure 1");
  }

  @Test
  @DisplayName(
    "measures with measures conditions but could not be found in the included section should fail")
  void measureWithoutMeasureConditions() {
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .measureConditionIds(Set.of("1"))
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
          .build());
    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .included(includedEntities)
        .build();

    assertThatIllegalArgumentException()
      .isThrownBy(() -> measureBuilder.from(tradeTariffCommodityResponse))
      .withMessage("Cannot find measure condition for measure condition 1");
  }

  @Test
  @DisplayName("measures with excise flag should be treated as duty measure")
  void shouldSetMeasureAsDutyMeasureIfSetAsExcise() {
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .isExcise(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .measureConditionIds(Set.of())
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
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
        .included(includedEntities)
        .build();

    List<Measure> measures = measureBuilder.from(tradeTariffCommodityResponse);

    assertThat(measures).hasSize(1);
    assertThat(measures.get(0).isTaxMeasure()).isEqualTo(true);
  }

  @Test
  @DisplayName("measures with VAT flag should be treated as duty measure")
  void shouldSetMeasureAsDutyMeasureIfSetAsVatMeasure() {
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .isVAT(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .measureConditionIds(Set.of())
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
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
        .included(includedEntities)
        .build();

    List<Measure> measures = measureBuilder.from(tradeTariffCommodityResponse);

    assertThat(measures).hasSize(1);
    assertThat(measures.get(0).isTaxMeasure()).isEqualTo(true);
  }

  @Test
  @DisplayName(
    "measures with excise flag and vat flag not set should not be treated as duty measure")
  void shouldSetNotMeasureAsDutyMeasureIfExciseOrVATFlagIsNotSet() {
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .isExcise(false)
          .isVAT(false)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .measureConditionIds(Set.of())
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
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
        .included(includedEntities)
        .build();

    List<Measure> measures = measureBuilder.from(tradeTariffCommodityResponse);

    assertThat(measures).hasSize(1);
    assertThat(measures.get(0).isTaxMeasure()).isEqualTo(false);
  }

  @Test
  @DisplayName("measures with tax value")
  void shouldSetTaxValue() {
    String dutyValue = "duty value";
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .measureConditionIds(Set.of())
          .dutyExpressionId("1")
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build(),
        DutyExpression.builder().id("1").base(dutyValue).build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
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
        .included(includedEntities)
        .build();

    List<Measure> measures = measureBuilder.from(tradeTariffCommodityResponse);

    assertThat(measures).hasSize(1);
    assertThat(measures.get(0).getDutyValue()).isPresent();
    assertThat(measures.get(0).getDutyValue()).contains(dutyValue);
  }

  @Test
  @DisplayName("measures with quota number")
  void shouldSetQuotaNumber() {
    String quotaNumber = "quota number";
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .isVAT(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .measureConditionIds(Set.of())
          .quotaNumber(quotaNumber)
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
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
        .included(includedEntities)
        .build();

    List<Measure> measures = measureBuilder.from(tradeTariffCommodityResponse);

    assertThat(measures).hasSize(1);
    assertThat(measures.get(0).getQuotaNumber()).isPresent();
    assertThat(measures.get(0).getQuotaNumber()).contains(quotaNumber);
  }

  @Test
  @DisplayName("measures with duty calculator overlay")
  void shouldUseOverlaysFromDutyCalculator() {
    String additionalCode = "4200";
    List<TradeTariffCommodityResponseIncludedEntity> includedEntities =
      List.of(
        CommodityMeasure.builder()
          .id("1")
          .isImport(true)
          .isVAT(true)
          .geographicalAreaId(GEOGRAPHICAL_AREA_ID)
          .measureTypeId(EXPECTED_MEASURE_TYPE_ID)
          .measureConditionIds(Set.of())
          .additionalCodeId("1")
          .build(),
        CommodityMeasureType.builder()
          .id(EXPECTED_MEASURE_TYPE_ID)
          .description("Measure Type 2")
          .build(),
        CommodityGeographicalArea.builder()
          .id(GEOGRAPHICAL_AREA_ID)
          .description("ERGA OMNES")
          .build(),
        CommodityAdditionalCode.builder()
          .id("1")
          .code(additionalCode)
          .description("Procyon lotor")
          .build());

    String additionalCodeOverlayText = "overlay text";
    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(
          TradeTariffCommodityResponseData.builder()
            .id("1234")
            .type("commodity")
            .formattedDescription("description")
            .goodsNomenclatureItemId(COMMODITY_CODE)
            .dutyCalculatorAdditionalCodes(
              List.of(
                DutyCalculatorAdditionalCode.builder()
                  .code(additionalCode)
                  .overlay(additionalCodeOverlayText)
                  .build()))
            .build())
        .included(includedEntities)
        .build();

    List<Measure> measures = measureBuilder.from(tradeTariffCommodityResponse);

    assertThat(measures).hasSize(1);
    assertThat(measures.get(0).getAdditionalCode()).isPresent();
    assertThat(measures.get(0).getAdditionalCode())
      .contains(
        AdditionalCode.builder()
          .code(additionalCode)
          .description(additionalCodeOverlayText)
          .build());
  }
}
