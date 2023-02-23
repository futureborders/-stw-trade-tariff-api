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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;

class DocumentCodeMeasureOptionTest {

  @Test
  void shouldReturnNullWhenDescriptionIsNull() {
    DocumentCodeMeasureOption documentCodeMeasureOption =
      DocumentCodeMeasureOption.builder()
        .documentCodeDescription(
          DocumentCodeDescription.builder()
            .documentCode("C055")
            .build())
        .totalNumberOfCertificates(1)
        .build();

    assertThat(documentCodeMeasureOption.getDescriptionOverlay())
      .isNull();
  }

  @Test
  void shouldReturnOriginalDescriptionWhenTotalCertificatesIsOne() {
    DocumentCodeMeasureOption documentCodeMeasureOption =
      DocumentCodeMeasureOption.builder()
        .documentCodeDescription(
          DocumentCodeDescription.builder()
            .descriptionOverlay("You need this document.")
            .documentCode("C055")
            .build())
        .totalNumberOfCertificates(1)
        .build();

    assertThat(documentCodeMeasureOption.getDescriptionOverlay())
      .isEqualTo("You need this document.");
  }

  @ParameterizedTest
  @ValueSource(strings = {"You need this document.", "you need this document."})
  void shouldReturnDecoratedDescriptionWhenTotalCertificatesIsMoreThanOne(
    String originalDescription) {
    DocumentCodeMeasureOption documentCodeMeasureOption =
      DocumentCodeMeasureOption.builder()
        .documentCodeDescription(
          DocumentCodeDescription.builder()
            .descriptionOverlay(originalDescription)
            .documentCode("C055")
            .build())
        .totalNumberOfCertificates(2)
        .build();

    assertThat(documentCodeMeasureOption.getDescriptionOverlay())
      .isEqualTo("Check if you need this document.");
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void shouldNotDecorateWhenDescriptionDoesNotStartWith_You_need(int totalCertificates) {
    DocumentCodeMeasureOption documentCodeMeasureOption =
      DocumentCodeMeasureOption.builder()
        .documentCodeDescription(
          DocumentCodeDescription.builder()
            .descriptionOverlay("Your goods are for scientific purposes.")
            .documentCode("C055")
            .build())
        .totalNumberOfCertificates(totalCertificates)
        .build();

    assertThat(documentCodeMeasureOption.getDescriptionOverlay())
      .isEqualTo("Your goods are for scientific purposes.");
  }

  @Test
  void shouldReturnAllFields() {
    Integer id = 100;
    String documentCode = "C055";

    MeasureOption documentCodeMeasureOption =
      DocumentCodeMeasureOption.builder()
        .documentCodeDescription(
          DocumentCodeDescription.builder()
            .id(id)
            .documentCode(documentCode)
            .descriptionOverlay("Your goods are for scientific purposes.")
            .build())
        .totalNumberOfCertificates(1)
        .build();

    SoftAssertions.assertSoftly(
      softly -> {
        softly.assertThat(documentCodeMeasureOption.getType())
          .isEqualTo(MeasureOptionType.CERTIFICATE);
        softly.assertThat(documentCodeMeasureOption.getId()).isEqualTo(String.valueOf(id));
        softly.assertThat(documentCodeMeasureOption.getCertificateCode()).isEqualTo(documentCode);
      });
  }
}
