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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;

class ExceptionMeasureOptionTest {

  @Test
  void shouldSetAllFields() {
    Integer id = 100;
    String documentCode = "C055";
    String descriptionOverlay = "Your goods are for scientific purposes.";

    MeasureOption exceptionMeasureOption =
      ExceptionMeasureOption.exceptionMeasureOptionBuilder()
        .documentCodeDescription(
          DocumentCodeDescription.builder()
            .id(id)
            .documentCode(documentCode)
            .descriptionOverlay(descriptionOverlay)
            .build())
        .build();

    SoftAssertions.assertSoftly(
      softly -> {
        softly
          .assertThat(exceptionMeasureOption.getType())
          .isEqualTo(MeasureOptionType.EXCEPTION);
        softly.assertThat(exceptionMeasureOption.getId()).isEqualTo(String.valueOf(id));
        softly.assertThat(exceptionMeasureOption.getCertificateCode()).isEqualTo(documentCode);
        softly
          .assertThat(exceptionMeasureOption.getDescriptionOverlay())
          .isEqualTo("If your goods are for scientific purposes, then your goods are exempt.");
      });
  }

  @Test
  void shouldNotDecorateExceptionDescriptionWhenDoesNotStartWith_Your() {
    String descriptionOverlay = "If your goods are not organic, you may need to show proof.";
    ExceptionMeasureOption exceptionMeasureOption =
      ExceptionMeasureOption.exceptionMeasureOptionBuilder()
        .documentCodeDescription(
          DocumentCodeDescription.builder().descriptionOverlay(descriptionOverlay).build())
        .build();

    assertThat(exceptionMeasureOption.getDescriptionOverlay()).isEqualTo(descriptionOverlay);
  }
}
