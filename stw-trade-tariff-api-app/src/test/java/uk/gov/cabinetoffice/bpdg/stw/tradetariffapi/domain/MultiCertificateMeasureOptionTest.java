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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;

public class MultiCertificateMeasureOptionTest {

  private static Stream<Arguments> documentCodeDescriptionCombinations() {
    return Stream.of(
        Arguments.of(
            null, "You need 2nd certificate document.", "You need 2nd certificate document."),
        Arguments.of(
            "You need 1st certificate document.", null, "You need 1st certificate document."),
        Arguments.of(
            "", "You need 2nd certificate document.", "You need 2nd certificate document."),
        Arguments.of(
            "You need 1st certificate document.", "", "You need 1st certificate document."),
        Arguments.of(null, null, null),
        Arguments.of("", "", null));
  }

  @Test
  void shouldReturnBothDocumentCodes() {
    MultiCertificateMeasureOption multiCertificateMeasureOption =
        MultiCertificateMeasureOption.builder()
            .certificate1(DocumentCodeDescription.builder().documentCode("C669").build())
            .certificate2(DocumentCodeDescription.builder().documentCode("C670").build())
            .build();

    assertThat(multiCertificateMeasureOption.getCertificateCode()).isEqualTo("C669 & C670");
  }

  @Test
  void shouldReturnCombinedDescriptions() {
    MultiCertificateMeasureOption multiCertificateMeasureOption =
        MultiCertificateMeasureOption.builder()
            .certificate1(
                DocumentCodeDescription.builder()
                    .documentCode("C669")
                    .descriptionOverlay("You need document C669 for this trade.")
                    .build())
            .certificate2(
                DocumentCodeDescription.builder()
                    .documentCode("C670")
                    .descriptionOverlay("You need document C670 for this trade.")
                    .build())
            .build();

    assertThat(multiCertificateMeasureOption.getDescriptionOverlay())
        .isEqualTo(
            "You need document C669 for this trade and you need document C670 for this trade.");
  }

  @ParameterizedTest
  @MethodSource("documentCodeDescriptionCombinations")
  void shouldReturnSingleDescriptionWhenOneIsEmptyOrNull(
      String firstCertDesc, String secondCertDesc, String expectedDesc) {
    MultiCertificateMeasureOption multiCertificateMeasureOption =
        MultiCertificateMeasureOption.builder()
            .certificate1(
                DocumentCodeDescription.builder()
                    .documentCode("C669")
                    .descriptionOverlay(firstCertDesc)
                    .build())
            .certificate2(
                DocumentCodeDescription.builder()
                    .documentCode("C670")
                    .descriptionOverlay(secondCertDesc)
                    .build())
            .build();

    assertThat(multiCertificateMeasureOption.getDescriptionOverlay()).isEqualTo(expectedDesc);
  }
}
