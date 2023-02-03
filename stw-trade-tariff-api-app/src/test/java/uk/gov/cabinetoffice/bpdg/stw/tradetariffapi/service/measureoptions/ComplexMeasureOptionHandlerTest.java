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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.IMPORT;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentaryMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionAndThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MultiCertificateMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.NegativeMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.WeightOrVolumeOrUnitBasedThresholdMeasureCondition;

@ExtendWith(MockitoExtension.class)
public class ComplexMeasureOptionHandlerTest {

  private static final String EXCEPTION_DOCUMENT_CODE_Y111 = "Y111";
  private static final String CERTIFICATE_DOCUMENT_CODE1 = "N111";
  private static final String THRESHOLD_REQUIREMENT =
      "<span>2.0</span> <abbr title='Kilogram'>kg</abbr>";
  private static final String THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT = "2.0";
  private static final String THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE = "KGM";

  private static final String CERTIFICATE1_DESCRIPTION_IN_API =
      "You need certificate description in content api";
  private static final String EXCEPTION_DESCRIPTION_IN_API =
      "Your goods exception description in content api";
  private static final String CERTIFICATE_DEFAULT_REQUIREMENT = "certificate default requirement";
  private static final String CERTIFICATE_DEFAULT_DESCRIPTION = "certificate default description";
  private static final String EXCEPTION_DEFAULT_REQUIREMENT = "exception default requirement";
  private static final String EXCEPTION_DEFAULT_DESCRIPTION = "exception default description";

  @Mock private DocumentCodeDescriptionRepository documentCodeDescriptionRepository;

  @InjectMocks private ComplexMeasureOptionHandler complexMeasureHandler;

  @Nested
  class GetDocumentCodeMeasureOptions {

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("no measures")
    void shouldReturnEmptyWhenNoMeasuresArePassed(Locale locale) {
      StepVerifier.create(complexMeasureHandler.getMeasureOption(null, IMPORT, locale))
          .verifyComplete();

      StepVerifier.create(complexMeasureHandler.getMeasureOption(List.of(), IMPORT, locale))
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("complex measures with document code descriptions in content api")
    void shouldGetComplexMeasuresWhenDocumentCodeDescriptions(Locale locale) {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          NegativeMeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .conditionDutyAmount(THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
              .conditionMeasurementUnitCode(THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          NegativeMeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                      .build()));

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_Y111), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_API)
                      .build()));

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      negativeMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE,
                      negativeMeasureConditionWithMeasureConditionE),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_API)
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder()
                                      .threshold(
                                          WeightOrVolumeOrUnitBasedThresholdMeasureCondition
                                              .builder()
                                              .id("5000")
                                              .conditionCode(secondMeasureConditionCode)
                                              .conditionDutyAmount(
                                                  THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
                                              .conditionMeasurementUnitCode(
                                                  THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
                                              .requirement(THRESHOLD_REQUIREMENT)
                                              .build())
                                      .locale(locale)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("complex measures with no document code descriptions")
    void shouldGetComplexMeasuresWhenNoDocumentCodeDescriptions(Locale locale) {

      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .conditionDutyAmount(THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
              .conditionMeasurementUnitCode(THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  anyList(), eq(IMPORT), eq(locale)))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .totalNumberOfCertificates(1)
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_DESCRIPTION)
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder()
                                      .threshold(
                                          WeightOrVolumeOrUnitBasedThresholdMeasureCondition
                                              .builder()
                                              .id("4000")
                                              .conditionCode(secondMeasureConditionCode)
                                              .conditionDutyAmount(
                                                  THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
                                              .conditionMeasurementUnitCode(
                                                  THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
                                              .requirement(THRESHOLD_REQUIREMENT)
                                              .build())
                                      .locale(locale)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should get first code description when there are multiple certificate document code descriptions configured")
    void
        shouldGetTheFirstDocumentCodeDescriptionWhenThereAreMultipleCertificateDocumentCodeDescriptionsConfigured(Locale locale) {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          NegativeMeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .conditionDutyAmount(THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
              .conditionMeasurementUnitCode(THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          NegativeMeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                      .build(),
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay("second description")
                      .build()));

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_Y111), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_API)
                      .build()));

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      negativeMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE,
                      negativeMeasureConditionWithMeasureConditionE),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_API)
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder()
                                      .threshold(
                                          WeightOrVolumeOrUnitBasedThresholdMeasureCondition
                                              .builder()
                                              .id("5000")
                                              .conditionCode(secondMeasureConditionCode)
                                              .conditionDutyAmount(
                                                  THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
                                              .conditionMeasurementUnitCode(
                                                  THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
                                              .requirement(THRESHOLD_REQUIREMENT)
                                              .build())
                                      .locale(locale)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should get first document code when multiple exception document code descriptions configured")
    void
        shouldGetTheFirstDocumentCodeDescriptionIfThereAreMoreThanOneExceptionDocumentCodeDescriptions(Locale locale) {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          NegativeMeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .conditionDutyAmount(THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
              .conditionMeasurementUnitCode(THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          NegativeMeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                      .build()));

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_Y111), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_API)
                      .build(),
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay("second description")
                      .build()));

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      negativeMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE,
                      negativeMeasureConditionWithMeasureConditionE),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_API)
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder()
                                      .threshold(
                                          WeightOrVolumeOrUnitBasedThresholdMeasureCondition
                                              .builder()
                                              .id("5000")
                                              .conditionCode(secondMeasureConditionCode)
                                              .conditionDutyAmount(
                                                  THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
                                              .conditionMeasurementUnitCode(
                                                  THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
                                              .requirement(THRESHOLD_REQUIREMENT)
                                              .build())
                                      .locale(locale)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should resolve document code description when only some of the document code descriptions configured")
    void
        shouldResolveDocumentCodeDescriptionWhenOnlySomeOfTheDocumentCodeDescriptionsAreConfigured(Locale locale) {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          NegativeMeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .conditionDutyAmount(THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
              .conditionMeasurementUnitCode(THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          NegativeMeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                      .build()));

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_Y111), IMPORT, locale))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      negativeMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE,
                      negativeMeasureConditionWithMeasureConditionE),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_API)
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_DESCRIPTION)
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder()
                                      .threshold(
                                          WeightOrVolumeOrUnitBasedThresholdMeasureCondition
                                              .builder()
                                              .id("5000")
                                              .conditionCode(secondMeasureConditionCode)
                                              .conditionDutyAmount(
                                                  THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
                                              .conditionMeasurementUnitCode(
                                                  THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
                                              .requirement(THRESHOLD_REQUIREMENT)
                                              .build())
                                      .locale(locale)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("should combine multiple disjoint certificates into a single measure option")
    void shouldCombineDisjointCertificatesIntoSingleMeasureOption(Locale locale) {
      MeasureConditionCode conditionCodeE = MeasureConditionCode.E;
      MeasureCondition certificateC672WithConditionCodeE =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(conditionCodeE)
              .documentCode("C672")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificateC669WithConditionCodeE =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(conditionCodeE)
              .documentCode("C669")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionY923WithConditionCodeE =
          DocumentaryMeasureCondition.builder()
              .id("3000")
              .conditionCode(conditionCodeE)
              .documentCode("Y923")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition thresholdWithConditionCodeE =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("4000")
              .conditionCode(conditionCodeE)
              .conditionDutyAmount(THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
              .conditionMeasurementUnitCode(THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithConditionCodeE =
          NegativeMeasureCondition.builder()
              .id("5000")
              .conditionCode(conditionCodeE)
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode conditionCodeI = MeasureConditionCode.I;
      MeasureCondition certificateC672WithConditionCodeI =
          DocumentaryMeasureCondition.builder()
              .id("6000")
              .conditionCode(conditionCodeI)
              .documentCode("C672")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificateC670WithConditionCodeI =
          DocumentaryMeasureCondition.builder()
              .id("7000")
              .conditionCode(conditionCodeI)
              .documentCode("C670")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificateY923WithConditionCodeI =
          DocumentaryMeasureCondition.builder()
              .id("8000")
              .conditionCode(conditionCodeI)
              .documentCode("Y923")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition thresholdWithConditionCodeI =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("9000")
              .conditionCode(conditionCodeI)
              .conditionDutyAmount(THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
              .conditionMeasurementUnitCode(THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithConditionCodeI =
          NegativeMeasureCondition.builder()
              .id("9001")
              .conditionCode(conditionCodeI)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  anyList(), eq(IMPORT), eq(locale)))
          .thenAnswer(
              (Answer<Flux<DocumentCodeDescription>>)
                  invocation -> {
                    List<String> documentCodes = invocation.getArgument(0);

                    return Flux.fromIterable(
                        documentCodes.stream()
                            .map(
                                documentCode ->
                                    DocumentCodeDescription.builder()
                                        .documentCode(documentCode)
                                        .descriptionOverlay(
                                            "You need the certificate "
                                                + documentCode
                                                + " description in content api")
                                        .build())
                            .collect(Collectors.toList()));
                  });

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateC672WithConditionCodeE,
                      certificateC669WithConditionCodeE,
                      exceptionY923WithConditionCodeE,
                      thresholdWithConditionCodeE,
                      negativeMeasureConditionWithConditionCodeE,
                      certificateC670WithConditionCodeI,
                      certificateC672WithConditionCodeI,
                      certificateY923WithConditionCodeI,
                      thresholdWithConditionCodeI,
                      negativeMeasureConditionWithConditionCodeI),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          ThresholdMeasureOption.builder()
                              .threshold(
                                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                                      .id("4000")
                                      .conditionCode(conditionCodeE)
                                      .conditionDutyAmount(
                                          THRESHOLD_REQUIREMENT_CONDITION_DUTY_AMOUNT)
                                      .conditionMeasurementUnitCode(
                                          THRESHOLD_REQUIREMENT_CONDITION_MEASUREMENT_UNIT_CODE)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build())
                              .locale(locale)
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C672")
                                      .descriptionOverlay(
                                          "You need the certificate C672 description in content api")
                                      .build())
                              .totalNumberOfCertificates(1)
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y923")
                                      .descriptionOverlay(
                                          "You need the certificate Y923 description in content api")
                                      .build())
                              .build(),
                          MultiCertificateMeasureOption.builder()
                              .certificate1(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C669")
                                      .descriptionOverlay(
                                          "You need the certificate C669 description in content api")
                                      .build())
                              .certificate2(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C670")
                                      .descriptionOverlay(
                                          "You need the certificate C670 description in content api")
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should order document codes on measure options based on type and if same type then on condition code")
    void shouldOrderBasedOnDocumentCodes(Locale locale) {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificate1MeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("C001")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificate2MeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("C002")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificate3MeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("C004")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exception1MeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("Y001")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificate1MeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("C002")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificate2MeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("C001")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificate3MeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("C003")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exception1MeasureConditionWithMeasureConditionE =
          DocumentaryMeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("Y001")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  anyList(), eq(IMPORT), eq(locale)))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificate1MeasureConditionWithMeasureConditionB,
                      certificate2MeasureConditionWithMeasureConditionB,
                      certificate3MeasureConditionWithMeasureConditionB,
                      exception1MeasureConditionWithMeasureConditionB,
                      certificate1MeasureConditionWithMeasureConditionE,
                      certificate2MeasureConditionWithMeasureConditionE,
                      certificate3MeasureConditionWithMeasureConditionE,
                      exception1MeasureConditionWithMeasureConditionE),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C001")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .totalNumberOfCertificates(2)
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C002")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .totalNumberOfCertificates(2)
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y001")
                                      .descriptionOverlay(EXCEPTION_DEFAULT_DESCRIPTION)
                                      .build())
                              .build(),
                          MultiCertificateMeasureOption.builder()
                              .certificate1(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C003")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .certificate2(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C004")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }
  }
}
