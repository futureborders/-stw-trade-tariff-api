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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExceptionAndThresholdMeasureOptionTest {

  @Test
  void shouldCombineExceptionAndWeightOrVolumeBasedThresholdDescriptions() {
    ExceptionAndThresholdMeasureOption exceptionAndThresholdMeasureOption =
        ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
            .exception(
                DocumentCodeDescription.builder()
                    .descriptionOverlay(
                        "Your shipment contains goods intended for scientific purposes, research or diagnostic samples.")
                    .build())
            .thresholdMeasure(
                ThresholdMeasureOption.builder()
                    .threshold(
                        WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                            .conditionDutyAmount("2.00")
                            .conditionMeasurementUnitCode("KGM")
                            .requirement("<span>2.00</span> <abbr title='Kilogram'>kg</abbr>")
                            .conditionCode(MeasureConditionCode.E)
                            .build())
                    .locale(Locale.EN)
                    .build())
            .build();

    assertThat(exceptionAndThresholdMeasureOption.getDescriptionOverlay())
        .isEqualTo(
            "If your shipment contains goods intended for scientific purposes, research or diagnostic samples and weigh less than 2 kilograms, then your goods are exempt.");
  }

  @Test
  void shouldCombineExceptionAndPriceBasedThresholdDescriptions() {
    ExceptionAndThresholdMeasureOption exceptionAndThresholdMeasureOption =
        ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
            .exception(
                DocumentCodeDescription.builder()
                    .descriptionOverlay(
                        "Your shipment contains goods intended for scientific purposes, research or diagnostic samples.")
                    .build())
            .thresholdMeasure(
                ThresholdMeasureOption.builder()
                    .threshold(
                        PriceBasedThresholdMeasureCondition.builder()
                            .conditionDutyAmount("2.00")
                            .conditionMonetaryUnitCode("GBP")
                            .requirement("<span>2.00</span> GBP")
                            .conditionCode(MeasureConditionCode.E)
                            .build())
                    .locale(Locale.EN)
                    .build())
            .build();

    assertThat(exceptionAndThresholdMeasureOption.getDescriptionOverlay())
        .isEqualTo(
            "If your shipment contains goods intended for scientific purposes, research or diagnostic samples and the value of your shipment is less than £2, then your goods are exempt.");
  }

  @Test
  void shouldNotDecorateExceptionDescriptionWhenDoesNotStartWith_Your() {
    ExceptionAndThresholdMeasureOption exceptionAndThresholdMeasureOption =
        ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
            .exception(
                DocumentCodeDescription.builder()
                    .descriptionOverlay(
                        "My shipment contains goods intended for scientific purposes, research or diagnostic samples.")
                    .build())
            .thresholdMeasure(
                ThresholdMeasureOption.builder()
                    .threshold(
                        WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                            .conditionDutyAmount("2.00")
                            .conditionMeasurementUnitCode("KGM")
                            .requirement("<span>2.00</span> <abbr title='Kilogram'>kg</abbr>")
                            .conditionCode(MeasureConditionCode.E)
                            .build())
                    .locale(Locale.EN)
                    .build())
            .build();

    assertThat(exceptionAndThresholdMeasureOption.getDescriptionOverlay())
        .isEqualTo(
            "My shipment contains goods intended for scientific purposes, research or diagnostic samples and weigh less than 2 kilograms, then your goods are exempt.");
  }

  @Test
  void shouldBeUnaffectedWhenExceptionDescriptionDoesNotEndWithA_Period() {
    ExceptionAndThresholdMeasureOption exceptionAndThresholdMeasureOption =
        ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
            .exception(
                DocumentCodeDescription.builder()
                    .descriptionOverlay(
                        "Your goods are intended for scientific purposes. Also, research or diagnostic samples is covered")
                    .build())
            .thresholdMeasure(
                ThresholdMeasureOption.builder()
                    .threshold(
                        WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                            .conditionDutyAmount("2.00")
                            .conditionMeasurementUnitCode("KGM")
                            .requirement("<span>2.00</span> <abbr title='Kilogram'>kg</abbr>")
                            .conditionCode(MeasureConditionCode.E)
                            .build())
                    .locale(Locale.EN)
                    .build())
            .build();

    assertThat(exceptionAndThresholdMeasureOption.getDescriptionOverlay())
        .isEqualTo(
            "If your goods are intended for scientific purposes. Also, research or diagnostic samples is covered and weigh less than 2 kilograms, then your goods are exempt.");
  }
}
