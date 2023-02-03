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

import static java.lang.String.format;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentaryMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Quota;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tariff;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tax;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@ExtendWith(MockitoExtension.class)
class DutyMeasureServiceTest {

  private static final String MEASURE_TYPE_ID = "1";
  private static final String DEFAULT_DESCRIPTION = "default description";
  private static final String DESCRIPTION_FROM_CONTENT_API = "description from API";
  private static final Locale LOCALE = Locale.EN;

  @Mock private MeasureTypeDescriptionRepository measureTypeDescriptionRepository;

  @InjectMocks private DutyMeasureService dutyMeasureService;

  @DisplayName("should ignore measures if tax value is not present for tax and tariffs")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldIgnoreMeasuresIfTaxValueIsNotPresentForDutiesAndTariffs(TradeType tradeType) {
    List<MeasureCondition> measureConditions =
        List.of(DocumentaryMeasureCondition.builder().build());
    Measure measure =
        Measure.builder()
            .dutyValue(null)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .verifyComplete();
  }

  @DisplayName("should include conditional measures if tax value is empty for tax and tariffs")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldIncludeMeasuresIfTaxValueIsEmptyForDutiesAndTariffsAsTheyAreConditionalDutyMeasures(
      TradeType tradeType) {
    List<MeasureCondition> measureConditions =
        List.of(DocumentaryMeasureCondition.builder().build());
    String dutyValue = "";
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionRepository
            .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                List.of(measure.getMeasureType().getId()), tradeType, LOCALE))
        .thenReturn(Flux.empty());

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DEFAULT_DESCRIPTION)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @DisplayName(
      "should return content when description has been setup for requested destination country with tariff details")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldReturnContentWhenDescriptionHasBeenSetupForDestinationCountryWithTariffDetails(
      TradeType tradeType) {
    List<MeasureCondition> measureConditions =
        List.of(
            DocumentaryMeasureCondition.builder().conditionCode(MeasureConditionCode.C).build());
    AdditionalCode additionalCode = AdditionalCode.builder().build();
    String quotaNumber = "quota number";
    String dutyValue = "value";
    GeographicalArea geographicalArea = GeographicalArea.builder().build();
    Measure measure =
        Measure.builder()
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .additionalCode(additionalCode)
            .quotaNumber(quotaNumber)
            .dutyValue(dutyValue)
            .geographicalArea(geographicalArea)
            .build();

    when(measureTypeDescriptionRepository
            .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                List.of(measure.getMeasureType().getId()), tradeType, LOCALE))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DESCRIPTION_FROM_CONTENT_API)
                .additionalCode(additionalCode)
                .value(dutyValue)
                .geographicalArea(geographicalArea)
                .quota(Quota.builder().number(quotaNumber).build())
                .build())
        .verifyComplete();
  }

  @DisplayName(
      "should return content when description has been setup for requested destination country with tax details")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldReturnContentWhenDescriptionHasBeenSetupForDestinationCountryWithTaxDetails(
      TradeType tradeType) {
    List<MeasureCondition> measureConditions =
        List.of(
            DocumentaryMeasureCondition.builder().conditionCode(MeasureConditionCode.C).build());
    AdditionalCode additionalCode = AdditionalCode.builder().build();
    GeographicalArea geographicalArea = GeographicalArea.builder().build();
    String dutyValue = "value";
    Measure measure =
        Measure.builder()
            .taxMeasure(true)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .additionalCode(additionalCode)
            .dutyValue(dutyValue)
            .geographicalArea(geographicalArea)
            .build();

    when(measureTypeDescriptionRepository
            .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                List.of(measure.getMeasureType().getId()), tradeType, LOCALE))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .expectNext(
            Tax.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DESCRIPTION_FROM_CONTENT_API)
                .additionalCode(additionalCode)
                .value(dutyValue)
                .geographicalArea(geographicalArea)
                .build())
        .verifyComplete();
  }

  @DisplayName(
      "should return content when description has been setup for more than one destination country")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldReturnContentWhenDescriptionHasBeenSetupForMoreThanOneDestinationCountry(
      TradeType tradeType) {
    String dutyValue = "tax value";
    List<MeasureCondition> measureConditions =
        List.of(DocumentaryMeasureCondition.builder().build());
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionRepository
            .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                List.of(measure.getMeasureType().getId()), tradeType, LOCALE))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DESCRIPTION_FROM_CONTENT_API)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @DisplayName(
      "should return content when no description has been setup for requested destination country")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldReturnContentWhenNoDescriptionHasBeenSetupForDestinationCountry(TradeType tradeType) {
    String dutyValue = "tax value";
    List<MeasureCondition> measureConditions =
        List.of(DocumentaryMeasureCondition.builder().build());
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionRepository
            .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                List.of(measure.getMeasureType().getId()), tradeType, LOCALE))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DESCRIPTION_FROM_CONTENT_API)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @DisplayName("should return content when no description has been setup for measure type")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldReturnContentWhenNoDescriptionHasBeenSetupForMeasureType(TradeType tradeType) {
    List<MeasureCondition> measureConditions =
        List.of(DocumentaryMeasureCondition.builder().build());
    String dutyValue = "tax value";
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionRepository
            .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                List.of(measure.getMeasureType().getId()), tradeType, LOCALE))
        .thenReturn(Flux.empty());

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DEFAULT_DESCRIPTION)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @DisplayName("should return content based on locale")
  @ParameterizedTest
  @EnumSource(Locale.class)
  void shouldReturnContentBasedOnLocale(Locale locale) {
    List<MeasureCondition> measureConditions =
        List.of(DocumentaryMeasureCondition.builder().build());
    String dutyValue = "tax value";
    TradeType tradeType = TradeType.IMPORT;
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionRepository
        .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
            List.of(measure.getMeasureType().getId()), tradeType, locale))
        .thenReturn(Flux.empty());

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, locale))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DEFAULT_DESCRIPTION)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @DisplayName("should raise error when more than one description has been setup")
  @ParameterizedTest
  @EnumSource(TradeType.class)
  void shouldRaiseErrorWhenMoreThanOneDescriptionHasBeenSetupForTheRequestedDestinationCountry(
      TradeType tradeType) {
    List<MeasureCondition> measureConditions =
        List.of(DocumentaryMeasureCondition.builder().build());
    String dutyValue = "tax value";
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionRepository
            .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                List.of(measure.getMeasureType().getId()), tradeType, LOCALE))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                    .build(),
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), tradeType, LOCALE))
        .expectErrorMatches(
            error ->
                error instanceof RuntimeException
                    && error
                        .getMessage()
                        .equals(
                            format(
                                "More than one measure type descriptions configured for measure type %s, locale %s",
                                measure.getMeasureType().getId(), LOCALE)))
        .verify();
  }
}
