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

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.IMPORT;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentaryMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.NegativeMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions.ComplexMeasureOptionHandler;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions.MultipleMeasureOptionsHandler;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions.SingleMeasureOptionHandler;

@ExtendWith(MockitoExtension.class)
public class MeasureOptionServiceTest {

  private static final String CDS_WAIVER = "999L";
  private static final NegativeMeasureCondition NEGATIVE_CONDITION =
      NegativeMeasureCondition.builder().build();

  @Mock private SingleMeasureOptionHandler singleMeasureOptionHandler;
  @Mock private ComplexMeasureOptionHandler complexMeasureOptionHandler;
  @Mock private MultipleMeasureOptionsHandler multipleMeasureOptionsHandler;

  private MeasureOptionService measureOptionService;

  @BeforeEach
  public void setUp() {
    measureOptionService =
        new MeasureOptionService(
            multipleMeasureOptionsHandler, singleMeasureOptionHandler, complexMeasureOptionHandler);
  }

  @Nested
  class GetMeasureOptions {

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldProcessMeasuresWithOneConditionCode(Locale locale) {
      MeasureConditionCode bMeasureConditionCode = MeasureConditionCode.B;
      String documentCode = "C123";
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(bMeasureConditionCode)
              .documentCode(documentCode)
              .requirement("certificate requirement")
              .build();

      MeasureOptions measureOptionForB =
          MeasureOptions.builder()
              .options(
                  List.of(
                      DocumentCodeMeasureOption.builder()
                          .totalNumberOfCertificates(1)
                          .documentCodeDescription(
                              DocumentCodeDescription.builder().documentCode(documentCode).build())
                          .build()))
              .build();
      Mockito.when(
              singleMeasureOptionHandler.getMeasureOption(
                  List.of(certificateMeasureConditionWithMeasureConditionB), IMPORT, locale))
          .thenReturn(Mono.just(measureOptionForB));

      StepVerifier.create(
              measureOptionService.getMeasureOptions(
                  List.of(certificateMeasureConditionWithMeasureConditionB, NEGATIVE_CONDITION),
                  IMPORT, locale))
          .expectNext(measureOptionForB)
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldProcessMeasuresWithMoreThanOneConditionCode(Locale locale) {
      String documentCode1 = "C123";
      String documentCode2 = "C223";

      MeasureConditionCode bMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(bMeasureConditionCode)
              .documentCode(documentCode1)
              .requirement("certificate requirement")
              .build();
      MeasureConditionCode cMeasureConditionCode = MeasureConditionCode.C;
      MeasureCondition certificateMeasureConditionWithMeasureConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(cMeasureConditionCode)
              .documentCode(documentCode2)
              .requirement("certificate requirement")
              .build();

      MeasureOptions measureOptionForB =
          MeasureOptions.builder()
              .options(
                  List.of(
                      DocumentCodeMeasureOption.builder()
                          .totalNumberOfCertificates(1)
                          .documentCodeDescription(
                              DocumentCodeDescription.builder().documentCode(documentCode1).build())
                          .build()))
              .build();
      MeasureOptions measureOptionForC =
          MeasureOptions.builder()
              .options(
                  List.of(
                      DocumentCodeMeasureOption.builder()
                          .totalNumberOfCertificates(1)
                          .documentCodeDescription(
                              DocumentCodeDescription.builder().documentCode(documentCode2).build())
                          .build()))
              .build();
      Mockito.when(
              multipleMeasureOptionsHandler.getMeasureOptions(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionC),
                  IMPORT,
                  locale))
          .thenReturn(Flux.just(measureOptionForB, measureOptionForC));

      StepVerifier.create(
              measureOptionService.getMeasureOptions(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      certificateMeasureConditionWithMeasureConditionC,
                      NEGATIVE_CONDITION),
                  IMPORT,
                  locale))
          .expectNext(measureOptionForB, measureOptionForC)
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldProcessMeasuresWhichSharesDocumentCodes(Locale locale) {
      String commonDocumentCode = "C111";
      String documentCode1 = "C123";
      String documentCode2 = "C223";

      MeasureConditionCode bMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(bMeasureConditionCode)
              .documentCode(documentCode1)
              .requirement("certificate requirement")
              .build();
      MeasureCondition commonCertificateWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(bMeasureConditionCode)
              .documentCode(commonDocumentCode)
              .requirement("certificate requirement")
              .build();
      MeasureConditionCode cMeasureConditionCode = MeasureConditionCode.C;
      MeasureCondition certificateWithMeasureConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(cMeasureConditionCode)
              .documentCode(documentCode2)
              .requirement("certificate requirement")
              .build();
      MeasureCondition commonCertificateWithMeasureConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(cMeasureConditionCode)
              .documentCode(commonDocumentCode)
              .requirement("certificate requirement")
              .build();

      MeasureOptions complexMeasureOption =
          MeasureOptions.builder()
              .options(
                  List.of(
                      DocumentCodeMeasureOption.builder()
                          .documentCodeDescription(DocumentCodeDescription.builder().build())
                          .build()))
              .build();
      Mockito.when(
              complexMeasureOptionHandler.getMeasureOption(
                  List.of(
                      certificateWithMeasureConditionB,
                      commonCertificateWithMeasureConditionB,
                      certificateWithMeasureConditionC,
                      commonCertificateWithMeasureConditionC),
                  IMPORT,
                  locale))
          .thenReturn(Mono.just(complexMeasureOption));

      StepVerifier.create(
              measureOptionService.getMeasureOptions(
                  List.of(
                      certificateWithMeasureConditionB,
                      commonCertificateWithMeasureConditionB,
                      certificateWithMeasureConditionC,
                      commonCertificateWithMeasureConditionC,
                      NEGATIVE_CONDITION),
                  IMPORT,
                  locale))
          .expectNext(complexMeasureOption)
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldTreatAsMultipleMeasuresWhen999LConditionCodesIsTheOnlyOneShared(Locale locale) {
      String documentCode1 = "C123";
      String documentCode2 = "C223";

      MeasureConditionCode bMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateMeasureConditionWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(bMeasureConditionCode)
              .documentCode(documentCode1)
              .requirement("certificate requirement")
              .build();
      MeasureCondition cdsWaiverOnConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1002")
              .conditionCode(bMeasureConditionCode)
              .documentCode("999L")
              .build();
      MeasureConditionCode cMeasureConditionCode = MeasureConditionCode.C;
      MeasureCondition certificateMeasureConditionWithMeasureConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(cMeasureConditionCode)
              .documentCode(documentCode2)
              .requirement("certificate requirement")
              .build();
      MeasureCondition cdsWaiverOnConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1002")
              .conditionCode(cMeasureConditionCode)
              .documentCode(CDS_WAIVER)
              .build();

      MeasureOptions measureOptionForB =
          MeasureOptions.builder()
              .options(
                  List.of(
                      DocumentCodeMeasureOption.builder()
                          .totalNumberOfCertificates(1)
                          .documentCodeDescription(
                              DocumentCodeDescription.builder().documentCode(documentCode1).build())
                          .build(),
                      ExceptionMeasureOption.builder()
                          .documentCodeDescription(
                              DocumentCodeDescription.builder().documentCode(CDS_WAIVER).build())
                          .build()))
              .build();
      MeasureOptions measureOptionForC =
          MeasureOptions.builder()
              .options(
                  List.of(
                      DocumentCodeMeasureOption.builder()
                          .totalNumberOfCertificates(1)
                          .documentCodeDescription(
                              DocumentCodeDescription.builder().documentCode(documentCode2).build())
                          .build(),
                      ExceptionMeasureOption.builder()
                          .documentCodeDescription(
                              DocumentCodeDescription.builder().documentCode(CDS_WAIVER).build())
                          .build()))
              .build();
      Mockito.when(
              multipleMeasureOptionsHandler.getMeasureOptions(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      cdsWaiverOnConditionB,
                      certificateMeasureConditionWithMeasureConditionC,
                      cdsWaiverOnConditionC),
                  IMPORT,
                  locale))
          .thenReturn(Flux.just(measureOptionForB, measureOptionForC));

      StepVerifier.create(
              measureOptionService.getMeasureOptions(
                  List.of(
                      certificateMeasureConditionWithMeasureConditionB,
                      cdsWaiverOnConditionB,
                      certificateMeasureConditionWithMeasureConditionC,
                      cdsWaiverOnConditionC,
                      NEGATIVE_CONDITION),
                  IMPORT,
                  locale))
          .expectNext(measureOptionForB, measureOptionForC)
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldTreatAsComplexMeasureWhenAnotherConditionCodeAlongWith999LAreShared(Locale locale) {
      String commonDocumentCode = "C111";
      String documentCode1 = "C123";
      String documentCode2 = "C223";

      MeasureConditionCode bMeasureConditionCode = MeasureConditionCode.B;
      MeasureCondition certificateWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(bMeasureConditionCode)
              .documentCode(documentCode1)
              .requirement("certificate requirement")
              .build();
      MeasureCondition commonCertificateWithMeasureConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(bMeasureConditionCode)
              .documentCode(commonDocumentCode)
              .requirement("certificate requirement")
              .build();
      MeasureCondition cdsWaiverOnConditionB =
          DocumentaryMeasureCondition.builder()
              .id("1002")
              .conditionCode(bMeasureConditionCode)
              .documentCode("999L")
              .build();
      MeasureConditionCode cMeasureConditionCode = MeasureConditionCode.C;
      MeasureCondition certificateWithMeasureConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(cMeasureConditionCode)
              .documentCode(documentCode2)
              .requirement("certificate requirement")
              .build();
      MeasureCondition commonCertificateWithMeasureConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1000")
              .conditionCode(cMeasureConditionCode)
              .documentCode(commonDocumentCode)
              .requirement("certificate requirement")
              .build();
      MeasureCondition cdsWaiverOnConditionC =
          DocumentaryMeasureCondition.builder()
              .id("1002")
              .conditionCode(bMeasureConditionCode)
              .documentCode("999L")
              .build();

      MeasureOptions complexMeasureOption =
          MeasureOptions.builder()
              .options(
                  List.of(
                      DocumentCodeMeasureOption.builder()
                          .documentCodeDescription(DocumentCodeDescription.builder().build())
                          .build()))
              .build();
      Mockito.when(
              complexMeasureOptionHandler.getMeasureOption(
                  List.of(
                      certificateWithMeasureConditionB,
                      commonCertificateWithMeasureConditionB,
                      cdsWaiverOnConditionB,
                      certificateWithMeasureConditionC,
                      commonCertificateWithMeasureConditionC,
                      cdsWaiverOnConditionC),
                  IMPORT,
                  locale))
          .thenReturn(Mono.just(complexMeasureOption));

      StepVerifier.create(
              measureOptionService.getMeasureOptions(
                  List.of(
                      certificateWithMeasureConditionB,
                      commonCertificateWithMeasureConditionB,
                      cdsWaiverOnConditionB,
                      certificateWithMeasureConditionC,
                      commonCertificateWithMeasureConditionC,
                      cdsWaiverOnConditionC,
                      NEGATIVE_CONDITION),
                  IMPORT,
                  locale))
          .expectNext(complexMeasureOption)
          .verifyComplete();
    }
  }
}
