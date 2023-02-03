/*
 * Copyright 2022 Crown Copyright (Single Trade Window)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionType.NEGATIVE;

import org.junit.jupiter.api.Test;

class NegativeMeasureConditionTest {

  @Test
  void shouldGetNegativeConditionType() {
    NegativeMeasureCondition negativeMeasureCondition = NegativeMeasureCondition.builder().build();
    assertThat(negativeMeasureCondition.getMeasureConditionType()).isEqualTo(NEGATIVE);
  }

  @Test
  void shouldReturnNullAsMeasureConditionKey() {
    NegativeMeasureCondition negativeMeasureCondition = NegativeMeasureCondition.builder().build();
    assertThat(negativeMeasureCondition.getMeasureConditionKey()).isNull();
  }

  @Test
  void shouldGetNegativeMeasureConditionFields() {
    String id = "122";
    String action = "Import/export not allowed after control";
    String condition = "B: Presentation of a certificate/licence/document";
    MeasureConditionCode conditionCode = MeasureConditionCode.B;

    NegativeMeasureCondition negativeMeasureCondition =
        NegativeMeasureCondition.builder()
            .id(id)
            .action(action)
            .condition(condition)
            .conditionCode(conditionCode)
            .requirement(null)
            .build();

    assertThat(negativeMeasureCondition.getId()).isEqualTo(id);
    assertThat(negativeMeasureCondition.getAction()).isEqualTo(action);
    assertThat(negativeMeasureCondition.getCondition()).isEqualTo(condition);
    assertThat(negativeMeasureCondition.getConditionCode()).isEqualTo(conditionCode);
    assertThat(negativeMeasureCondition.getRequirement()).isEqualTo(null);
  }
}
