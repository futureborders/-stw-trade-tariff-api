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
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptionType.THRESHOLD;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.PRICE_BASED;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.PRICE_PER_UNIT_BASED;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.UNIT_BASED;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.VOLUME_BASED;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.WEIGHT_BASED;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ThresholdMeasureOptionTest {

  @Nested
  class WeightOrVolumeOrUnitBasedThresholdMeasureConditions {
    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldReturnCorrectDescriptionForMaxType(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100")
                      .conditionMeasurementUnitCode(MeasureUnit.LTR.name())
                      .requirement("<span>100.00</span> <abbr title='Litre'>l</abbr>")
                      .conditionCode(MeasureConditionCode.E)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              locale == Locale.CY
                  ? "If your shipment tbc less than 100 tbc, then your goods are exempt."
                  : "If your shipment is less than 100 litres, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldReturnCorrectDescriptionForMinType(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100")
                      .conditionMeasurementUnitCode(MeasureUnit.LTR.name())
                      .requirement("<span>100.00</span> <abbr title='Litre'>l</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              locale == Locale.CY
                  ? "If your shipment tbc more than 100 tbc, then your goods are exempt."
                  : "If your shipment is more than 100 litres, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldIgnoreDecimalPlacesOfQuantity(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100.0")
                      .conditionMeasurementUnitCode(MeasureUnit.LTR.name())
                      .requirement("<span>100.00</span> <abbr title='Litre'>l</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              locale == Locale.CY
                  ? "If your shipment tbc more than 100 tbc, then your goods are exempt."
                  : "If your shipment is more than 100 litres, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldSetPropertiesForVolumeBasedThreshold(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100.0")
                      .conditionMeasurementUnitCode(MeasureUnit.LTR.name())
                      .requirement("<span>100.00</span> <abbr title='Litre'>l</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getType()).isEqualTo(THRESHOLD);
      assertThat(thresholdMeasureOption.getSubtype()).isEqualTo(VOLUME_BASED);
      assertThat(thresholdMeasureOption.getUnit()).isNull();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldSetPropertiesForWeightBasedThreshold(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("2.0")
                      .conditionMeasurementUnitCode(MeasureUnit.KGM.name())
                      .requirement("<span>2.00</span> <abbr title='Kilogram'>kg</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getType()).isEqualTo(THRESHOLD);
      assertThat(thresholdMeasureOption.getSubtype()).isEqualTo(WEIGHT_BASED);
      assertThat(thresholdMeasureOption.getUnit()).isNull();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldSetPropertiesForUnitBasedThreshold(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  WeightOrVolumeOrUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("2.0")
                      .conditionMeasurementUnitCode(MeasureUnit.GP1.name())
                      .requirement("<span>2</span> <abbr title='Units'>units</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getType()).isEqualTo(THRESHOLD);
      assertThat(thresholdMeasureOption.getSubtype()).isEqualTo(UNIT_BASED);
      assertThat(thresholdMeasureOption.getUnit()).isNull();
    }
  }

  @Nested
  class PriceBasedThresholdMeasureConditions {
    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldReturnCorrectDescriptionForMaxType(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PriceBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .requirement("<span>100.00</span> GBP")
                      .conditionCode(MeasureConditionCode.E)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              "If the value of your shipment is less than £100, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldReturnCorrectDescriptionForMinType(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PriceBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .requirement("<span>100.00</span> GBP")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              "If the value of your shipment is more than £100, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldIgnoreDecimalPlacesOfAmount(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PriceBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100.0")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .requirement("<span>100.00</span> GBP")
                      .conditionCode(MeasureConditionCode.E)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              "If the value of your shipment is less than £100, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldSetPropertiesForPriceBasedThreshold(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PriceBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("100.0")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .requirement("<span>100.00</span> GBP")
                      .conditionCode(MeasureConditionCode.E)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getType()).isEqualTo(THRESHOLD);
      assertThat(thresholdMeasureOption.getSubtype()).isEqualTo(PRICE_BASED);
      assertThat(thresholdMeasureOption.getUnit()).isNull();
    }
  }

  @Nested
  class PricePerUnitBasedThresholdMeasureConditions {
    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldReturnCorrectDescriptionForMaxType(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PricePerUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("250")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .conditionMeasurementUnitCode(MeasureUnit.NAR.name())
                      .requirement(
                          "<span>250.00</span> GBP / <abbr title='Number of items'>p/st</abbr>")
                      .conditionCode(MeasureConditionCode.E)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              locale == Locale.CY
                  ? "If the value of your shipment is less than £250 / tbc, then your goods are exempt."
                  : "If the value of your shipment is less than £250 / item, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldReturnCorrectDescriptionForMinType(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PricePerUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("250")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .conditionMeasurementUnitCode(MeasureUnit.NAR.name())
                      .requirement(
                          "<span>250.00</span> GBP / <abbr title='Number of items'>p/st</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              locale == Locale.CY
                  ? "If the value of your shipment is more than £250 / tbc, then your goods are exempt."
                  : "If the value of your shipment is more than £250 / item, then your goods are exempt.");
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void shouldIgnoreDecimalPlacesOfAmount(Locale locale) {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PricePerUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("250.0")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .conditionMeasurementUnitCode(MeasureUnit.NAR.name())
                      .requirement(
                          "<span>250.00</span> GBP / <abbr title='Number of items'>p/st</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(locale)
              .build();

      assertThat(thresholdMeasureOption.getDescriptionOverlay())
          .isEqualTo(
              locale == Locale.CY
                  ? "If the value of your shipment is more than £250 / tbc, then your goods are exempt."
                  : "If the value of your shipment is more than £250 / item, then your goods are exempt.");
    }

    @Test
    void shouldSetPropertiesForPricePerUnitBasedThresholdForEnglish() {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PricePerUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("250.0")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .conditionMeasurementUnitCode(MeasureUnit.NAR.name())
                      .requirement(
                          "<span>250.00</span> GBP / <abbr title='Number of items'>p/st</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(Locale.EN)
              .build();

      assertThat(thresholdMeasureOption.getType()).isEqualTo(THRESHOLD);
      assertThat(thresholdMeasureOption.getSubtype()).isEqualTo(PRICE_PER_UNIT_BASED);
      assertThat(thresholdMeasureOption.getUnit()).isEqualTo("item");
    }

    @Test
    void shouldSetPropertiesForPricePerUnitBasedThresholdForWelsh() {
      ThresholdMeasureOption thresholdMeasureOption =
          ThresholdMeasureOption.builder()
              .threshold(
                  PricePerUnitBasedThresholdMeasureCondition.builder()
                      .conditionDutyAmount("250.0")
                      .conditionMonetaryUnitCode(MonetaryUnitCode.GBP.name())
                      .conditionMeasurementUnitCode(MeasureUnit.NAR.name())
                      .requirement(
                          "<span>250.00</span> GBP / <abbr title='Number of items'>p/st</abbr>")
                      .conditionCode(MeasureConditionCode.F)
                      .build())
              .locale(Locale.CY)
              .build();

      assertThat(thresholdMeasureOption.getType()).isEqualTo(THRESHOLD);
      assertThat(thresholdMeasureOption.getSubtype()).isEqualTo(PRICE_PER_UNIT_BASED);
      assertThat(thresholdMeasureOption.getUnit()).isEqualTo("tbc");
    }
  }
}
