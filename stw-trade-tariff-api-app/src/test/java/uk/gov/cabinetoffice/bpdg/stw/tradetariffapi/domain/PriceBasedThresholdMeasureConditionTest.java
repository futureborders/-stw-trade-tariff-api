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

class PriceBasedThresholdMeasureConditionTest {

  @Test
  void shouldGetThresholdConditionType() {
    PriceBasedThresholdMeasureCondition priceBasedThresholdMeasureCondition =
        PriceBasedThresholdMeasureCondition.builder().build();
    assertThat(priceBasedThresholdMeasureCondition.getMeasureConditionType()).isEqualTo(THRESHOLD);
  }

  @Test
  void shouldReturnRequirementFieldAsMeasureConditionKey() {
    String requirement = "requirement";
    PriceBasedThresholdMeasureCondition priceBasedThresholdMeasureCondition =
        PriceBasedThresholdMeasureCondition.builder().requirement(requirement).build();
    assertThat(priceBasedThresholdMeasureCondition.getMeasureConditionKey()).isEqualTo(requirement);
  }

  @Test
  void shouldGetPriceBasedThresholdMeasureConditionFields() {
    String id = "122";
    String action = "Entry into free circulation allowed";
    String condition =
        "E: The quantity or the price per unit declared, as appropriate, is equal or less than the specified maximum, or presentation of the required document";
    MeasureConditionCode conditionCode = MeasureConditionCode.E;
    String requirement = "<span>250.00</span> GBP";
    String conditionDutyAmount = "250";
    String conditionMonetaryUnitCode = "GBP";

    PriceBasedThresholdMeasureCondition priceBasedThresholdMeasureCondition =
        PriceBasedThresholdMeasureCondition.builder()
            .id(id)
            .action(action)
            .condition(condition)
            .conditionCode(conditionCode)
            .requirement(requirement)
            .conditionDutyAmount(conditionDutyAmount)
            .conditionMonetaryUnitCode(conditionMonetaryUnitCode)
            .build();

    assertThat(priceBasedThresholdMeasureCondition.getId()).isEqualTo(id);
    assertThat(priceBasedThresholdMeasureCondition.getAction()).isEqualTo(action);
    assertThat(priceBasedThresholdMeasureCondition.getCondition()).isEqualTo(condition);
    assertThat(priceBasedThresholdMeasureCondition.getConditionCode()).isEqualTo(conditionCode);
    assertThat(priceBasedThresholdMeasureCondition.getRequirement()).isEqualTo(requirement);
    assertThat(priceBasedThresholdMeasureCondition.getConditionDutyAmount())
        .isEqualTo(conditionDutyAmount);
    assertThat(priceBasedThresholdMeasureCondition.getConditionMonetaryUnitCode())
        .isEqualTo(conditionMonetaryUnitCode);
  }
}
