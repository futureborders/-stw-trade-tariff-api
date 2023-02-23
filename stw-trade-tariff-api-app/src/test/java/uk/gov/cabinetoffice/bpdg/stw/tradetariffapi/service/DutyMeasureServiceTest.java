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
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Quota;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tariff;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tax;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@ExtendWith(MockitoExtension.class)
class DutyMeasureServiceTest {

  private static final String MEASURE_TYPE_ID = "1";
  private static final String DEFAULT_DESCRIPTION = "default description";
  private static final String DESCRIPTION_FROM_DB = "description from DB";

  @Mock private MeasureTypeDescriptionContentRepo measureTypeDescriptionContentRepo;

  @InjectMocks private DutyMeasureService dutyMeasureService;

  @Test
  @DisplayName("should ignore measures if tax value is not present for tax and tariffs")
  void shouldIgnoreMeasuresIfTaxValueIsNotPresentForDutiesAndTariffs() {
    List<MeasureCondition> measureConditions = List.of(MeasureCondition.builder().build());
    Measure measure =
        Measure.builder()
            .dutyValue(null)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), UkCountry.GB))
        .verifyComplete();
  }

  @Test
  @DisplayName("should include conditional measures if tax value is empty for tax and tariffs")
  void shouldIncludeMeasuresIfTaxValueIsEmptyForDutiesAndTariffsAsTheyAreConditionalDutyMeasures() {
    List<MeasureCondition> measureConditions = List.of(MeasureCondition.builder().build());
    String dutyValue = "";
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionContentRepo.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of(measure.getMeasureType().getId()), AppConfig.LOCALE, true))
        .thenReturn(Flux.empty());

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), UkCountry.GB))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DEFAULT_DESCRIPTION)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "should return content when description has been setup for requested destination country with tariff details")
  void shouldReturnContentWhenDescriptionHasBeenSetupForDestinationCountryWithTariffDetails() {
    List<MeasureCondition> measureConditions =
        List.of(MeasureCondition.builder().conditionCode(MeasureConditionCode.C).build());
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

    when(measureTypeDescriptionContentRepo.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of(measure.getMeasureType().getId()), AppConfig.LOCALE, true))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_DB)
                    .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), UkCountry.GB))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DESCRIPTION_FROM_DB)
                .additionalCode(additionalCode)
                .value(dutyValue)
                .geographicalArea(geographicalArea)
                .quota(Quota.builder().number(quotaNumber).build())
                .build())
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "should return content when description has been setup for requested destination country with tax details")
  void shouldReturnContentWhenDescriptionHasBeenSetupForDestinationCountryWithTaxDetails() {
    List<MeasureCondition> measureConditions =
        List.of(MeasureCondition.builder().conditionCode(MeasureConditionCode.C).build());
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

    when(measureTypeDescriptionContentRepo.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of(measure.getMeasureType().getId()), AppConfig.LOCALE, true))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_DB)
                    .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), UkCountry.GB))
        .expectNext(
            Tax.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DESCRIPTION_FROM_DB)
                .additionalCode(additionalCode)
                .value(dutyValue)
                .geographicalArea(geographicalArea)
                .build())
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "should return content when description has been setup for more than one destination country")
  void shouldReturnContentWhenDescriptionHasBeenSetupForMoreThanOneDestinationCountry() {
    String dutyValue = "tax value";
    List<MeasureCondition> measureConditions = List.of(MeasureCondition.builder().build());
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionContentRepo.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of(measure.getMeasureType().getId()), AppConfig.LOCALE, true))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_DB)
                    .destinationCountryRestrictions(Set.of(UkCountry.GB))
                    .build(),
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_DB)
                    .destinationCountryRestrictions(Set.of(UkCountry.XI))
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), UkCountry.GB))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DESCRIPTION_FROM_DB)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "should return content when no description has been setup for requested destination country")
  void shouldReturnContentWhenNoDescriptionHasBeenSetupForDestinationCountry() {
    String dutyValue = "tax value";
    List<MeasureCondition> measureConditions = List.of(MeasureCondition.builder().build());
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionContentRepo.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of(measure.getMeasureType().getId()), AppConfig.LOCALE, true))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_DB)
                    .destinationCountryRestrictions(Set.of(UkCountry.XI))
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), UkCountry.GB))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DEFAULT_DESCRIPTION)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @Test
  @DisplayName("should return content when no description has been setup for measure type")
  void shouldReturnContentWhenNoDescriptionHasBeenSetupForMeasureType() {
    List<MeasureCondition> measureConditions = List.of(MeasureCondition.builder().build());
    String dutyValue = "tax value";
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    when(measureTypeDescriptionContentRepo.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of(measure.getMeasureType().getId()), AppConfig.LOCALE, true))
        .thenReturn(Flux.empty());

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), UkCountry.GB))
        .expectNext(
            Tariff.builder()
                .measureTypeId(MEASURE_TYPE_ID)
                .text(DEFAULT_DESCRIPTION)
                .value(dutyValue)
                .build())
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "should raise error when more than one description has been setup for the requested destination country")
  void shouldRaiseErrorWhenMoreThanOneDescriptionHasBeenSetupForTheRequestedDestinationCountry() {
    List<MeasureCondition> measureConditions = List.of(MeasureCondition.builder().build());
    String dutyValue = "tax value";
    Measure measure =
        Measure.builder()
            .dutyValue(dutyValue)
            .measureType(
                MeasureType.builder().id(MEASURE_TYPE_ID).description(DEFAULT_DESCRIPTION).build())
            .measureConditions(measureConditions)
            .build();

    Locale locale = AppConfig.LOCALE;
    UkCountry destinationUkCountry = UkCountry.GB;
    when(measureTypeDescriptionContentRepo.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of(measure.getMeasureType().getId()), locale, true))
        .thenReturn(
            Flux.just(
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_DB)
                    .destinationCountryRestrictions(Set.of(destinationUkCountry))
                    .build(),
                MeasureTypeDescription.builder()
                    .measureTypeId(MEASURE_TYPE_ID)
                    .descriptionOverlay(DESCRIPTION_FROM_DB)
                    .destinationCountryRestrictions(Set.of(destinationUkCountry))
                    .build()));

    StepVerifier.create(
            dutyMeasureService.getTariffsAndTaxesMeasures(List.of(measure), destinationUkCountry))
        .expectErrorMatches(
            error ->
                error instanceof RuntimeException
                    && error
                        .getMessage()
                        .equals(
                            format(
                                "More than one measure type descriptions configured for measure type %s, locale %s, destination country %s",
                                measure.getMeasureType().getId(), locale, destinationUkCountry)))
        .verify();
  }
}
