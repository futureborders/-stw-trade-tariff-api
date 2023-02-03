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

class PricePerUnitBasedThresholdMeasureConditionTest {

  @Test
  void shouldGetThresholdConditionType() {
    PricePerUnitBasedThresholdMeasureCondition pricePerUnitBasedThresholdMeasureCondition =
        PricePerUnitBasedThresholdMeasureCondition.builder().build();
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getMeasureConditionType())
        .isEqualTo(THRESHOLD);
  }

  @Test
  void shouldReturnRequirementFieldAsMeasureConditionKey() {
    String requirement = "requirement";
    PricePerUnitBasedThresholdMeasureCondition pricePerUnitBasedThresholdMeasureCondition =
        PricePerUnitBasedThresholdMeasureCondition.builder().requirement(requirement).build();
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getMeasureConditionKey())
        .isEqualTo(requirement);
  }

  @Test
  void shouldGetPricePerUnitBasedThresholdMeasureConditionFields() {
    String id = "122";
    String action = "Entry into free circulation allowed";
    String condition =
        "E: The quantity or the price per unit declared, as appropriate, is equal or less than the specified maximum, or presentation of the required document";
    MeasureConditionCode conditionCode = MeasureConditionCode.E;
    String requirement = "<span>250.00</span> GBP / <abbr title='Number of items'>p/st</abbr>";
    String conditionDutyAmount = "250";
    String conditionMonetaryUnitCode = "GBP";
    String conditionMeasurementUnitCode = "NAR";

    PricePerUnitBasedThresholdMeasureCondition pricePerUnitBasedThresholdMeasureCondition =
        PricePerUnitBasedThresholdMeasureCondition.builder()
            .id(id)
            .action(action)
            .condition(condition)
            .conditionCode(conditionCode)
            .requirement(requirement)
            .conditionDutyAmount(conditionDutyAmount)
            .conditionMonetaryUnitCode(conditionMonetaryUnitCode)
            .conditionMeasurementUnitCode(conditionMeasurementUnitCode)
            .build();

    assertThat(pricePerUnitBasedThresholdMeasureCondition.getId()).isEqualTo(id);
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getAction()).isEqualTo(action);
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getCondition()).isEqualTo(condition);
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getConditionCode())
        .isEqualTo(conditionCode);
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getRequirement()).isEqualTo(requirement);
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getConditionDutyAmount())
        .isEqualTo(conditionDutyAmount);
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getConditionMonetaryUnitCode())
        .isEqualTo(conditionMonetaryUnitCode);
    assertThat(pricePerUnitBasedThresholdMeasureCondition.getConditionMeasurementUnitCode())
        .isEqualTo(conditionMeasurementUnitCode);
  }
}
