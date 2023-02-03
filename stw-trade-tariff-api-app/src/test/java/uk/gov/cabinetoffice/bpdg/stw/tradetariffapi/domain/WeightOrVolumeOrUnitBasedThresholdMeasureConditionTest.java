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
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionType.THRESHOLD;

import org.junit.jupiter.api.Test;

class WeightOrVolumeOrUnitBasedThresholdMeasureConditionTest {

  @Test
  void shouldGetThresholdConditionType() {
    WeightOrVolumeOrUnitBasedThresholdMeasureCondition weightOrVolumeOrUnitBasedThresholdMeasureCondition =
        WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder().build();
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getMeasureConditionType())
        .isEqualTo(THRESHOLD);
  }

  @Test
  void shouldReturnRequirementFieldAsMeasureConditionKey() {
    String requirement = "requirement";
    WeightOrVolumeOrUnitBasedThresholdMeasureCondition weightOrVolumeOrUnitBasedThresholdMeasureCondition =
        WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder().requirement(requirement).build();
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getMeasureConditionKey())
        .isEqualTo(requirement);
  }

  @Test
  void shouldGetWeightOrVolumeOrUnitBasedThresholdMeasureConditionFields() {
    String id = "122";
    String action = "Entry into free circulation allowed";
    String condition =
        "E: The quantity or the price per unit declared, as appropriate, is equal or less than the specified maximum, or presentation of the required document";
    MeasureConditionCode conditionCode = MeasureConditionCode.E;
    String requirement = "<span>100.00</span> <abbr title='Litre'>l</abbr>";
    String conditionDutyAmount = "100";
    String conditionMeasurementUnitCode = "LTR";

    WeightOrVolumeOrUnitBasedThresholdMeasureCondition weightOrVolumeOrUnitBasedThresholdMeasureCondition =
        WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
            .id(id)
            .action(action)
            .condition(condition)
            .conditionCode(conditionCode)
            .requirement(requirement)
            .conditionDutyAmount(conditionDutyAmount)
            .conditionMeasurementUnitCode(conditionMeasurementUnitCode)
            .build();

    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getId()).isEqualTo(id);
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getAction()).isEqualTo(action);
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getCondition()).isEqualTo(condition);
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getConditionCode())
        .isEqualTo(conditionCode);
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getRequirement())
        .isEqualTo(requirement);
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getConditionDutyAmount())
        .isEqualTo(conditionDutyAmount);
    assertThat(weightOrVolumeOrUnitBasedThresholdMeasureCondition.getConditionMeasurementUnitCode())
        .isEqualTo(conditionMeasurementUnitCode);
  }
}
