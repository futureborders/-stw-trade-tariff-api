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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

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
  private static final String EXCEPTION_DEFAULT_REQUIREMENT = "exception default requirement";
  private static final String THRESHOLD_REQUIREMENT =
      "<span>2.0</span> <abbr title='Kilogram'>kg</abbr>";

  @Mock private DocumentCodeDescriptionRepository documentCodeDescriptionRepository;

  @InjectMocks private SingleMeasureOptionHandler singleMeasureOptionHandler;

  @Nested
  class GetDocumentCodeMeasureOptions {

    @Test
    @DisplayName("no measures")
    void shouldReturnEmptyWhenNoMeasuresArePassed() {
      StepVerifier.create(singleMeasureOptionHandler.getMeasureOption(null, UkCountry.GB))
          .verifyComplete();

      StepVerifier.create(singleMeasureOptionHandler.getMeasureOption(List.of(), UkCountry.GB))
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures containing a single certificate")
    void shouldProcessSimpleMeasuresWithSingleCertificate() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(certificateMeasureCondition), UkCountry.GB))
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
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures with multiple certificates")
    void shouldProcessSimpleMeasuresWithMultipleCertificates() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition1 =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificateMeasureCondition2 =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE2)
              .requirement(CERTIFICATE2_DESCRIPTION_IN_DB)
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                      .build()));
      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE2), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .documentCode(CERTIFICATE_DOCUMENT_CODE2)
                      .descriptionOverlay(CERTIFICATE2_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(certificateMeasureCondition1, certificateMeasureCondition2),
                  UkCountry.GB))
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
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(2)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build()
                          ))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures containing an exception")
    void shouldProcessSimpleMeasuresWithException() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
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
              List.of(EXCEPTION_DOCUMENT_CODE_C084), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay(
                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(2)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(
                                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures containing an threshold")
    void shouldProcessSimpleMeasuresWithThreshold() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureCondition =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(measureConditionCode)
              .requirement(THRESHOLD_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
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
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      thresholdMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                          ThresholdMeasureOption.builder()
                              .threshold(
                                  MeasureCondition.builder()
                                      .id("5000")
                                      .conditionCode(measureConditionCode)
                                      .requirement(THRESHOLD_REQUIREMENT)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("ignore threshold if it not as expected")
    void shouldProcessSimpleMeasuresAndIgnoreThresholdIfItIsNotAsExpected() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition thresholdMeasureCondition =
          MeasureCondition.builder()
              .id("4000")
              .conditionCode(measureConditionCode)
              .requirement("<span>2.0</span>")
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
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
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      thresholdMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
    @DisplayName("simple measures with document code descriptions not defined in DB")
    void shouldUseDocumentCodeDescriptionsInMeasureIfDocumentCodeDescriptionsNotDefinedInDB() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(Flux.empty());

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_C084), AppConfig.LOCALE, true))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures with only some of the document codes defined in DB")
    void
        shouldUseDocumentCodeDescriptionFromMeasureIfOnlySomeOfThenHaveDocumentCodeDescriptionsDefinedInDB() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(CERTIFICATE_DOCUMENT_CODE1), AppConfig.LOCALE, true))
          .thenReturn(Flux.empty());

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_C084), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay(
                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                      .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
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
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(2)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(
                                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                                      .destinationCountryRestrictions(
                                          Set.of(UkCountry.GB, UkCountry.XI))
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures having GB and XI document code descriptions defined")
    void shouldReturnCountrySpecificDocumentCodeDescriptions() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
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
                      .descriptionOverlay("GB overlay")
                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_C084), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(3)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay("GB description")
                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(4)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay(
                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
                  UkCountry.XI))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(1)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(2)
                                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                                      .descriptionOverlay(CERTIFICATE1_DESCRIPTION_IN_DB)
                                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(4)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(
                                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures without country specific document code descriptions defined")
    void
        shouldUseMeasureDocumentCodeDescriptionWhenNoDocumentCodeDescriptionFoundForSpecificCountry() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
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
                      .descriptionOverlay("GB overlay")
                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_C084), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(3)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay("GB description")
                      .destinationCountryRestrictions(Set.of(UkCountry.GB))
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
                  UkCountry.XI))
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
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("simple measures having multiple document code descriptions defined")
    void shouldReturnFirstDocumentCodeDescriptionsIfMultipleFound() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificateMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(CERTIFICATE_DOCUMENT_CODE1)
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exceptionMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
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
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(2)
                      .documentCode(CERTIFICATE_DOCUMENT_CODE1)
                      .descriptionOverlay("second description")
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build()));

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
              List.of(EXCEPTION_DOCUMENT_CODE_C084), AppConfig.LOCALE, true))
          .thenReturn(
              Flux.just(
                  DocumentCodeDescription.builder()
                      .id(3)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay(
                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build(),
                  DocumentCodeDescription.builder()
                      .id(4)
                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                      .descriptionOverlay("second description")
                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                      .build()));

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exceptionMeasureCondition,
                      certificateMeasureCondition,
                      negativeMeasureCondition),
                  UkCountry.XI))
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
                                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .id(3)
                                      .documentCode(EXCEPTION_DOCUMENT_CODE_C084)
                                      .descriptionOverlay(
                                          "Your goods are intended for scientific purposes, research or diagnostic samples")
                                      .destinationCountryRestrictions(Set.of(UkCountry.XI))
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }

    @Test
    @DisplayName("should order document codes on measure options based on type and if same type then on condition code")
    void shouldOrderDocumentCodes() {
      MeasureConditionCode measureConditionCode = MeasureConditionCode.A;
      MeasureCondition certificate1MeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("C013")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition certificate2MeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("C012")
              .requirement(CERTIFICATE_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exception1MeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("Y124")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition exception2MeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("Y123")
              .requirement(EXCEPTION_DEFAULT_REQUIREMENT)
              .build();
      MeasureCondition negativeMeasureCondition =
          MeasureCondition.builder()
              .conditionCode(measureConditionCode)
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          List.of("C012"), AppConfig.LOCALE, true))
          .thenReturn(Flux.empty());
      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          List.of("C013"), AppConfig.LOCALE, true))
          .thenReturn(Flux.empty());

      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          List.of("Y123"), AppConfig.LOCALE, true))
          .thenReturn(Flux.empty());
      when(documentCodeDescriptionRepository.findByDocumentCodeInAndLocaleAndPublished(
          List.of("Y124"), AppConfig.LOCALE, true))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(
                      exception1MeasureCondition,
                      exception2MeasureCondition,
                      certificate1MeasureCondition,
                      certificate2MeasureCondition,
                      negativeMeasureCondition),
                  UkCountry.GB))
          .expectNext(
              MeasureOptions.builder()
                  .options(
                      List.of(
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(2)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C012")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .build(),
                          DocumentCodeMeasureOption.builder()
                              .totalNumberOfCertificates(2)
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("C013")
                                      .descriptionOverlay(CERTIFICATE_DEFAULT_REQUIREMENT)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y123")
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .build(),
                          ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                              .documentCodeDescription(
                                  DocumentCodeDescription.builder()
                                      .documentCode("Y124")
                                      .descriptionOverlay(EXCEPTION_DEFAULT_REQUIREMENT)
                                      .build())
                              .build()))
                  .build())
          .verifyComplete();
    }
  }
}
