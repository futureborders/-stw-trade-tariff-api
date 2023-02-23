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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("lgtm[java/non-static-nested-class]")
public class MeasureConditionTest {

  private static final String THRESHOLD_REQUIREMENT =
      "<span title='2.0 '>2.00</span> <abbr title='Kilogram'>kg</abbr>";

  @Nested
  class GetMeasureConditionType {

    @Test
    void shouldCategorizeADocumentAsCertificateWhenItDoesNotStartWithYAndIsNotDisguisedException() {
      MeasureCondition measureCondition = MeasureCondition.builder().documentCode("N111").build();
      MeasureConditionType measureConditionType = measureCondition.getMeasureConditionType();
      assertThat(measureConditionType).isEqualTo(MeasureConditionType.CERTIFICATE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"C084", "999L", "9020", "9021", "9009", "9010", "9011", "9015", "9016"})
    void shouldCategorizeADocumentAsExceptionWhenItIsOneOfTheDisguisedException(
        String documentCode) {
      MeasureCondition measureCondition =
          MeasureCondition.builder().documentCode(documentCode).build();
      MeasureConditionType measureConditionType = measureCondition.getMeasureConditionType();
      assertThat(measureConditionType).isEqualTo(MeasureConditionType.EXCEPTION);
    }

    @Test
    void shouldCategorizeADocumentAsThresholdWhenThereIsARequirementAndNoDocumentCodeAssociated() {
      MeasureCondition measureCondition =
          MeasureCondition.builder().requirement(THRESHOLD_REQUIREMENT).build();
      MeasureConditionType measureConditionType = measureCondition.getMeasureConditionType();
      assertThat(measureConditionType).isEqualTo(MeasureConditionType.THRESHOLD);
    }

    @Test
    void shouldCategorizeADocumentAsNegativeWhenThereIsNoRequirementAndNoDocumentCodeAssociated() {
      MeasureCondition measureCondition =
          MeasureCondition.builder().action("Import/export not allowed after control").build();

      MeasureConditionType measureConditionType = measureCondition.getMeasureConditionType();
      assertThat(measureConditionType).isEqualTo(MeasureConditionType.NEGATIVE);
    }

    @Test
    void
        shouldCategorizeADocumentAsNegativeWhenThereIsNoRequirementAndDocumentCodeAssociatedIsEmpty() {
      MeasureCondition measureCondition =
          MeasureCondition.builder()
              .documentCode("")
              .action("Import/export not allowed after control")
              .build();

      MeasureConditionType measureConditionType = measureCondition.getMeasureConditionType();
      assertThat(measureConditionType).isEqualTo(MeasureConditionType.NEGATIVE);
    }
  }
}
