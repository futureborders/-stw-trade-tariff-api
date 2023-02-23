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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionAndThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MultiCertificateMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@ExtendWith(MockitoExtension.class)
public class ComplexMeasureOptionHandlerTest {

  private static final String EXCEPTION_DOCUMENT_CODE_Y111 = "Y111";
  private static final String CERTIFICATE_DOCUMENT_CODE1 = "N111";
  private static final String THRESHOLD_REQUIREMENT =
      "<span>2.0</span> <abbr title='Kilogram'>kg</abbr>";
  private static final String CERTIFICATE1_DESCRIPTION_IN_DB =
      "You need certificate description in DB";
  private static final String EXCEPTION_DESCRIPTION_IN_DB =
      "Your goods exception description in DB";
  private static final String CERTIFICATE_DEFAULT_REQUIREMENT = "certificate default requirement";
  private static final String EXCEPTION_DEFAULT_REQUIREMENT = "exception default requirement";

  @Mock private DocumentCodeDescriptionRepository documentCodeDescriptionRepository;

  @InjectMocks private ComplexMeasureOptionHandler complexMeasureHandler;

  @Nested
  class GetDocumentCodeMeasureOptions {

    @Test
    @DisplayName("no measures")
    void shouldReturnEmptyWhenNoMeasuresArePassed() {
      StepVerifier.create(complexMeasureHandler.getMeasureOption(null, UkCountry.GB))
          .verifyComplete();

      StepVerifier.create(complexMeasureHandler.getMeasureOption(List.of(), UkCountry.GB))
          .verifyComplete();
    }

    @Test
    @DisplayName("complex measures with document code descriptions in DB")
    void shouldGetComplexMeasuresWhenDocumentCodeDescriptionsInDB() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(1)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_Y111), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .id(2)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder().threshold(MeasureCondition.builder()
                                      .id("5000")
                                      .conditionCode(secondMeasureConditionCode)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build()).build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("ignore exception and threshold when threshold is not as expected")
    void shouldDiscardExceptionAndThresholdOptionOnComplexMeasuresWhenThresholdIsNotInTheSameFormatAsExpected() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .requirement("<span>2.0</span>")
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(1)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("complex measures with no document code descriptions in DB")
    void shouldGetComplexMeasuresWhenNoDocumentCodeDescriptionsInDB() {

      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              anyList(), eq(AppConfig.LOCALE), eq(true)))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE),
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .totalNumberOfCertificates(1)
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder().threshold(MeasureCondition.builder()
                                      .id("4000")
                                      .conditionCode(secondMeasureConditionCode)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build()).build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("should get document code description for specific destination country")
    void shouldGetDocumentCodeDescriptionForSpecificDestinationCountry() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(1)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay("XI description")
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_Y111), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(3)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(4)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay("XI description")
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .id(3)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder().threshold(MeasureCondition.builder()
                                      .id("5000")
                                      .conditionCode(secondMeasureConditionCode)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build()).build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "should ignore document code description if none is configured for the destination country")
    void shouldIgnoreDocumentCodeDescriptionIfNoneIsConfiguredForTheDestinationCountry() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(1)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_Y111), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(4)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build()));

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE),
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .thresholdMeasure(ThresholdMeasureOption.builder().threshold(thresholdMeasureConditionWithMeasureConditionE).build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "should get first code description when there are multiple certificate document code descriptions configured")
    void
        shouldGetTheFirstDocumentCodeDescriptionWhenThereAreMultipleCertificateDocumentCodeDescriptionsConfigured() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(1)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay("second description")
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_Y111), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(3)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .id(3)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder().threshold(MeasureCondition.builder()
                                      .id("5000")
                                      .conditionCode(secondMeasureConditionCode)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build()).build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "should get first document code when multiple exception document code descriptions configured")
    void
        shouldGetTheFirstDocumentCodeDescriptionIfThereAreMoreThanOneExceptionDocumentCodeDescriptions() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(1)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_Y111), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(3)
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .id(2)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder().threshold(MeasureCondition.builder()
                                      .id("5000")
                                      .conditionCode(secondMeasureConditionCode)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build()).build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "should resolve document code description when only some of the document code descriptions configured")
    void
    shouldResolveDocumentCodeDescriptionWhenOnlySomeOfTheDocumentCodeDescriptionsAreConfigured() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificateMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(secondMeasureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(1)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          List.of(EXCEPTION_DOCUMENT_CODE_Y111), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.empty());

      StepVerifier.create(
              complexMeasureHandler.getMeasureOption(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      exceptionMeasureConditionWithMeasureConditionB,
                      negativeMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionE,
                      thresholdMeasureConditionWithMeasureConditionE,
                      negativeMeasureConditionWithMeasureConditionE),
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                                      .build())
                              .build(),
                          ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                              .exception(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_Y111)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .thresholdMeasure(
                                  ThresholdMeasureOption.builder().threshold(MeasureCondition.builder()
                                      .id("5000")
                                      .conditionCode(secondMeasureConditionCode)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build()).build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("should combine multiple disjoint certificates into a single measure option")
    void shouldCombineDisjointCertificatesIntoSingleMeasureOption() {
      MeasureConditionCode conditionCodeE = MeasureConditionCode.E;
      MeasureCondition certificateC672WithConditionCodeE =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(conditionCodeE)
              .documentCode("C672")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificateC669WithConditionCodeE =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(conditionCodeE)
              .documentCode("C669")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionY923WithConditionCodeE =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(conditionCodeE)
              .documentCode("Y923")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdWithConditionCodeE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(conditionCodeE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithConditionCodeE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(conditionCodeE)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode conditionCodeI = MeasureConditionCode.I;
      MeasureCondition certificateC672WithConditionCodeI =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(conditionCodeI)
              .documentCode("C672")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificateC670WithConditionCodeI =
          MeasureCondition.builder()
              .id("7000")
              .conditionCode(conditionCodeI)
              .documentCode("C670")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificateY923WithConditionCodeI =
          MeasureCondition.builder()
              .id("8000")
              .conditionCode(conditionCodeI)
              .documentCode("Y923")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdWithConditionCodeI =
          MeasureCondition.builder()
              .id("9000")
              .conditionCode(conditionCodeI)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithConditionCodeI =
          MeasureCondition.builder()
              .id("9001")
              .conditionCode(conditionCodeI)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              anyList(), eq(AppConfig.LOCALE), eq(true)))
          .thenAnswer(
              (Answer<Flux<DocumentCodeDescription>>)
                  invocation -> {
                    List<String> documentCodes = invocation.getArgument(0);

                    return Flux.fromIterable(
                        IntStream.range(0, documentCodes.size())
                            .mapToObj(
                                i ->
                                    DocumentCodeDescription.builder()
                                        .id(i + 1)
                                        .documentCode(documentCodes.get(i))
                                        .descriptionOverlay(
                                            "You need the certificate "
                                                + documentCodes.get(i)
                                                + " description in DB")
                                        .destinationCountryRestrictions(
                                            Set.of(UkCountry.GB, UkCountry.XI))
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          ThresholdMeasureOption.builder()
                              .threshold(
                                  MeasureCondition.builder()
                                      .id("9000")
                                      .conditionCode(conditionCodeE)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build())
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode("C672")
                                      .descriptionOverlay(
                                          "You need the certificate C672 description in DB")
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .totalNumberOfCertificates(1)
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode("Y923")
                                      .descriptionOverlay(
                                          "You need the certificate Y923 description in DB")
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build(),
                          MultiCertificateMeasureOption.builder()
                              .certificate1(
                                  DocumentCodeDescription.builder()
                                      .id(1)
                                      .documentCode("C669")
                                      .descriptionOverlay(
                                          "You need the certificate C669 description in DB")
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .certificate2(
                                  DocumentCodeDescription.builder()
                                      .id(2)
                                      .documentCode("C670")
                                      .descriptionOverlay(
                                          "You need the certificate C670 description in DB")
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "should ignore document code descriptions if not configured for destination whilst building multiple certificates measure option")
    void
        shouldNotUseDocumentCodeDescriptionForOtherDestinationForBuildingMultiCertificateMeasureOption() {
      MeasureConditionCode conditionCodeE = MeasureConditionCode.E;
      MeasureCondition certificateC672WithConditionCodeE =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(conditionCodeE)
              .documentCode("C672")
              .requirement("C672 " + CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificateC669WithConditionCodeE =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(conditionCodeE)
              .documentCode("C669")
              .requirement("C669 " + CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionY923WithConditionCodeE =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(conditionCodeE)
              .documentCode("Y923")
              .requirement("Y923 " + EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdWithConditionCodeE =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(conditionCodeE)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithConditionCodeE =
          MeasureCondition.builder()
              .id("5000")
              .conditionCode(conditionCodeE)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionCode conditionCodeI = MeasureConditionCode.I;
      MeasureCondition certificateC672WithConditionCodeI =
          MeasureCondition.builder()
              .id("6000")
              .conditionCode(conditionCodeI)
              .documentCode("C672")
              .requirement("C672 " + CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificateC670WithConditionCodeI =
          MeasureCondition.builder()
              .id("7000")
              .conditionCode(conditionCodeI)
              .documentCode("C670")
              .requirement("C670 " + CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificateY923WithConditionCodeI =
          MeasureCondition.builder()
              .id("8000")
              .conditionCode(conditionCodeI)
              .documentCode("Y923")
              .requirement("Y923 " + EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdWithConditionCodeI =
          MeasureCondition.builder()
              .id("9000")
              .conditionCode(conditionCodeI)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureConditionWithConditionCodeI =
          MeasureCondition.builder()
              .id("9001")
              .conditionCode(conditionCodeI)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              any(List.class), eq(AppConfig.LOCALE), eq(true)))
          .thenAnswer(
              (Answer<Flux<DocumentCodeDescription>>)
                  invocation -> {
                    List<String> documentCodes = invocation.getArgument(0);
                    return Flux.fromIterable(
                        documentCodes.stream()
                            .map(
                                dcd ->
                                    DocumentCodeDescription.builder()
                                        .id(2)
                                        .documentCode(dcd)
                                        .descriptionOverlay(
                                            "You need the certificate "
                                                + dcd
                                                + " description in DB")
                                        .destinationCountryRestrictions(Set.of(UkCountry.XI))
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          ThresholdMeasureOption.builder()
                              .threshold(
                                  MeasureCondition.builder()
                                      .id("9000")
                                      .conditionCode(conditionCodeE)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build())
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C672")
                                      .descriptionOverlay("C672 " + CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .totalNumberOfCertificates(1)
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y923")
                                      .descriptionOverlay("Y923 " + EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .build(),
                          MultiCertificateMeasureOption.builder()
                              .certificate1(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C669")
                                      .descriptionOverlay("C669 " + CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .certificate2(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C670")
                                      .descriptionOverlay("C670 " + CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("should order document codes on measure options based on type and if same type then on condition code")
    void shouldOrderBasedOnDocumentCodes() {
      MeasureConditionCode firstMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificate1MeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("C001")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificate2MeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("C002")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificate3MeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("1000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("C004")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exception1MeasureConditionWithMeasureConditionB =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("Y001")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();

      MeasureConditionCode secondMeasureConditionCode = MeasureConditionCode.E;
      MeasureCondition certificate1MeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("C002")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificate2MeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("C001")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificate3MeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("3000")
              .conditionCode(secondMeasureConditionCode)
              .documentCode("C003")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exception1MeasureConditionWithMeasureConditionE =
          MeasureCondition.builder()
              .id("2000")
              .conditionCode(firstMeasureConditionCode)
              .documentCode("Y001")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          anyList(), eq(AppConfig.LOCALE), eq(true)))
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
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C001")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .totalNumberOfCertificates(2)
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C002")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .totalNumberOfCertificates(2)
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y001")
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .build(),
                          MultiCertificateMeasureOption.builder()
                              .certificate1(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C003")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .certificate2(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C004")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

  }
}
