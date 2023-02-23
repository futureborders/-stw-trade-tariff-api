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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class ThresholdMeasureOptionTest {

  @Test
  void shouldReturnCorrectDescriptionForMaxType() {
    ThresholdMeasureOption thresholdMeasureOption =
      ThresholdMeasureOption.builder()
        .threshold(
          MeasureCondition.builder()
            .requirement("<span>100.00</span> <abbr title='Litre'>l</abbr>")
            .conditionCode(MeasureConditionCode.E)
            .build())
        .build();

    assertThat(thresholdMeasureOption.getDescriptionOverlay())
      .isEqualTo("If your shipment is less than 100 litres, then your goods are exempt.");
  }

  @Test
  void shouldReturnCorrectDescriptionForMinType() {
    ThresholdMeasureOption thresholdMeasureOption =
      ThresholdMeasureOption.builder()
        .threshold(
          MeasureCondition.builder()
            .requirement("<span>100.00</span> <abbr title='Litre'>l</abbr>")
            .conditionCode(MeasureConditionCode.F)
            .build())
        .build();

    assertThat(thresholdMeasureOption.getDescriptionOverlay())
      .isEqualTo("If your shipment is more than 100 litres, then your goods are exempt.");
  }

  @Test
  void shouldThrowExceptionWhenPatternUnrecognised() {
    ThresholdMeasureOption.ThresholdMeasureOptionBuilder thresholdMeasureOptionBuilder =
      ThresholdMeasureOption.builder()
        .threshold(
          MeasureCondition.builder()
            .requirement("<div>100.00</div> <abbr title='Litre'>l</abbr>")
            .conditionCode(MeasureConditionCode.E)
            .build());

    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(thresholdMeasureOptionBuilder::build)
      .withMessage(
        "Measure condition with requirement <div>100.00</div> <abbr title='Litre'>l</abbr> does not match expected format <span>(\\d*\\.?\\d*)</span> <abbr title='(.*)'>.*</abbr>");
  }
}
