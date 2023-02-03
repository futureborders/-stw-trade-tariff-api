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

import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.IMPORT;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentaryMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MonetaryUnitCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.NegativeMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.PriceBasedThresholdMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.WeightOrVolumeOrUnitBasedThresholdMeasureCondition;

@ExtendWith(MockitoExtension.class)
public class SingleMeasureOptionHandlerTest {

  private static final String EXCEPTION_DOCUMENT_CODE_C084 = "C084";
  private static final String CERTIFICATE_DOCUMENT_CODE1 = "N111";
  private static final String CERTIFICATE_DOCUMENT_CODE2 = "C014";
  private static final String CERTIFICATE1_DESCRIPTION_IN_DB =
      "You need certificate description in DB";
  private static final String CERTIFICATE2_DESCRIPTION_IN_DB =
      "You need certificate2 description in DB";
  private static final String CERTIFICATE_DEFAULT_REQUIREMENT = "certificate default requirement";
  private static final String CERTIFICATE_DEFAULT_DESCRIPTION = "certificate default description";
  private static final String EXCEPTION_DEFAULT_REQUIREMENT = "exception default requirement";
  private static final String EXCEPTION_DEFAULT_DESCRIPTION = "exception default description";

  @Mock private DocumentCodeDescriptionRepository documentCodeDescriptionRepository;

  @InjectMocks private SingleMeasureOptionHandler singleMeasureOptionHandler;

  @Nested
  class GetDocumentCodeMeasureOptions {

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("no measures")
    void shouldReturnEmptyWhenNoMeasuresArePassed(Locale locale) {
      StepVerifier.create(singleMeasureOptionHandler.getMeasureOption(null, IMPORT, locale))
          .verifyComplete();

      StepVerifier.create(singleMeasureOptionHandler.getMeasureOption(List.of(), IMPORT, locale))
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures containing a single certificate")
    void shouldProcessSimpleMeasuresWithSingleCertificate(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(certificateMeasureCondition), IMPORT, locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures with multiple certificates")
    void shouldProcessSimpleMeasuresWithMultipleCertificates(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition1 =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificateMeasureCondition2 =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE2)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build()));
      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE2), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE2)
                      .descriptionOverlay(CERTIFICATE2_DESCRIPTION_IN_DB)
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(certificateMeasureCondition1, certificateMeasureCondition2),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(2)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE2)
                                      .descriptionOverlay(CERTIFICATE2_DESCRIPTION_IN_DB)
                                      .build())
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(2)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures containing an exception")
    void shouldProcessSimpleMeasuresWithException(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureCondition =
          NegativeMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build()));

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_C084), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay(
                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(
                                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures containing thresholds")
    void shouldProcessSimpleMeasuresWithThresholds(Locale locale) {
      final String thresholdRequirement = "<span>2.0</span> <abbr title='Kilogram'>kg</abbr>";
      final String conditionAmountDuty = "2.0";
      final String conditionMeasurementUnitCode = "KGM";

      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition thresholdMeasureCondition1 =
          WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
              .id("4000")
              .conditionCode(measureConditionCode)
              .conditionDutyAmount(conditionAmountDuty)
              .conditionMeasurementUnitCode(conditionMeasurementUnitCode)
              .requirement(thresholdRequirement)
              .build();
      MeasureCondition thresholdMeasureCondition2 =
          PriceBasedThresholdMeasureCondition.builder()
              .id("4001")
              .conditionCode(measureConditionCode)
              .conditionDutyAmount(conditionAmountDuty)
              .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
              .requirement(thresholdRequirement)
              .build();
      MeasureCondition negativeMeasureCondition =
          NegativeMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      thresholdMeasureCondition1,
                      thresholdMeasureCondition2,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .build())
                              .build(),
                          ThresholdMeasureOption.builder()
                              .threshold(
                                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                                      .id("4000")
                                      .conditionCode(measureConditionCode)
                                      .conditionDutyAmount(conditionAmountDuty)
                                      .conditionMeasurementUnitCode(conditionMeasurementUnitCode)
                                      .requirement(thresholdRequirement)
                                      .build())
                              .locale(locale)
                              .build(),
                          ThresholdMeasureOption.builder()
                              .threshold(
                                  PriceBasedThresholdMeasureCondition.builder()
                                      .id("4001")
                                      .conditionCode(measureConditionCode)
                                      .conditionDutyAmount(conditionAmountDuty)
                                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                                      .requirement(thresholdRequirement)
                                      .build())
                              .locale(locale)
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures with document code descriptions not defined in DB")
    void shouldUseDocumentCodeDescriptionsInMeasureIfDocumentCodeDescriptionsNotDefinedInDB(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureCondition =
          NegativeMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(Flux.empty());

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_C084), IMPORT, locale))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_DESCRIPTION)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures with only some of the document codes defined in DB")
    void
        shouldUseDocumentCodeDescriptionFromMeasureIfOnlySomeOfThenHaveDocumentCodeDescriptionsDefinedInDB(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureCondition =
          NegativeMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(Flux.empty());

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_C084), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay(
                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(
                                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures having document code descriptions defined")
    void shouldReturnCountrySpecificDocumentCodeDescriptions(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureCondition =
          NegativeMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay("GB overlay")
                      .build()));

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_C084), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay("GB description")
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                                      .descriptionOverlay("GB overlay")
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay("GB description")
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("simple measures having multiple document code descriptions defined")
    void shouldReturnFirstDocumentCodeDescriptionsIfMultipleFound(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exceptionMeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureCondition =
          NegativeMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(CERTIFICATE_DOCUMENT_CODE1), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build(),
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay("second description")
                      .build()));

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of(EXCEPTION_DOCUMENT_CODE_C084), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay(
                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                      .build(),
                  DocumentCodeDescription.builder()
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay("second description")
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(
                                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should order document codes on measure options based on type and if same type then on condition code")
    void shouldOrderDocumentCodes(Locale locale) {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificate1MeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("C013")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition certificate2MeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("C012")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .description(CERTIFICATE_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exception1MeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("Y124")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition exception2MeasureCondition =
          DocumentaryMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("Y123")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .description(EXCEPTION_DEFAULT_DESCRIPTION)
              .build();
      MeasureCondition negativeMeasureCondition =
          NegativeMeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of("C012"), IMPORT, locale))
          .thenReturn(Flux.empty());
      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of("C013"), IMPORT, locale))
          .thenReturn(Flux.empty());

      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of("Y123"), IMPORT, locale))
          .thenReturn(Flux.empty());
      when(documentCodeDescriptionRepository
              .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
                  List.of("Y124"), IMPORT, locale))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exception1MeasureCondition,
                      exception2MeasureCondition,
                      certificate1MeasureCondition,
                      certificate2MeasureCondition,
                      negativeMeasureCondition),
                  IMPORT,
                  locale))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(2)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C012")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(2)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C013")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_DESCRIPTION)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y123")
                                      .descriptionOverlay(EXCEPTION_DEFAULT_DESCRIPTION)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y124")
                                      .descriptionOverlay(EXCEPTION_DEFAULT_DESCRIPTION)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }
  }
}
