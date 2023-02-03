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

import static java.lang.String.format;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.UNIT_BASED;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.VOLUME_BASED;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOptionSubType.WEIGHT_BASED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThresholdMeasureOption implements MeasureOption {

  public static final String WEIGHT_VOLUME_DESCRIPTION_START = "If your shipment ";
  public static final String PRICE_DESCRIPTION_START = "If ";
  private static final String NO_UNIT_CODE="NO_UNIT_CODE";

  public static final String PRICE_BASED_DESCRIPTION =
      PRICE_DESCRIPTION_START
          + "the value of your shipment is %s than %s%s, then your goods are exempt.";

  private static final String WEIGHT_VOLUME_DESCRIPTION_FORMAT =
      WEIGHT_VOLUME_DESCRIPTION_START + "%s %s than %s %s, then your goods are exempt.";

  public static final String PRICE_PER_UNIT_BASED_DESCRIPTION =
      PRICE_DESCRIPTION_START
          + "the value of your shipment is %s than %s%s / %s, then your goods are exempt.";

  private final MeasureOptionType type;
  private final ThresholdMeasureOptionSubType subtype;
  private final String unit;

  @JsonIgnore private final ThresholdMeasureCondition threshold;
  @JsonIgnore private final Locale locale;

  private final MeasureConditionCode conditionCode;

  @Builder
  public ThresholdMeasureOption(@NonNull ThresholdMeasureCondition threshold, @NonNull Locale locale) {
    this.type = MeasureOptionType.THRESHOLD;
    this.threshold = threshold;
    this.conditionCode = threshold.getConditionCode();
    this.locale = locale;

    if (threshold instanceof PricePerUnitBasedThresholdMeasureCondition) {
      this.subtype = ThresholdMeasureOptionSubType.PRICE_PER_UNIT_BASED;
      String conditionMeasurementUnitCode = ((PricePerUnitBasedThresholdMeasureCondition) threshold).getConditionMeasurementUnitCode();
      String measureUnitCode =
          StringUtils.isNotBlank(conditionMeasurementUnitCode)
              ? conditionMeasurementUnitCode
              : NO_UNIT_CODE;

      this.unit =
          Optional.ofNullable(measureUnitCode)
              .map(unitCode -> MeasureUnit.valueOf(unitCode).getUnit(locale, 1))
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          format(
                              "Not able to determine unit for unitcode %s",
                              conditionMeasurementUnitCode)));
    } else if (threshold instanceof PriceBasedThresholdMeasureCondition) {
      this.subtype = ThresholdMeasureOptionSubType.PRICE_BASED;
      this.unit = null;
    } else {
      String weightOrVolumeOrUnitBasedThresholdMeasureCondition =
          ((WeightOrVolumeOrUnitBasedThresholdMeasureCondition) threshold)
              .getConditionMeasurementUnitCode();
      String measureUnitCode =
          StringUtils.isNotBlank(weightOrVolumeOrUnitBasedThresholdMeasureCondition)
              ? weightOrVolumeOrUnitBasedThresholdMeasureCondition
              : NO_UNIT_CODE;
      MeasureUnit measureUnit = MeasureUnit.valueOf(measureUnitCode);
      if (measureUnit.isVolumeBased()){
        this.subtype = VOLUME_BASED;
      } else if (measureUnit.isWeightBased()) {
        this.subtype = WEIGHT_BASED;
      } else if (measureUnit.isUnitBased()){
        this.subtype = UNIT_BASED;
      } else {
        throw new IllegalStateException(format("Cannot determine subtype based on measure unit %s", measureUnit.name()));
      }
      this.unit = null;
    }
  }

  @Override
  public String getDescriptionOverlay() {
    if (threshold instanceof PricePerUnitBasedThresholdMeasureCondition) {
      PricePerUnitBasedThresholdMeasureCondition thresholdMeasureCondition =
          (PricePerUnitBasedThresholdMeasureCondition) threshold;
      int quantity = Double.valueOf(thresholdMeasureCondition.getConditionDutyAmount()).intValue();
      final String conditionMeasurementUnitCode =
          thresholdMeasureCondition.getConditionMeasurementUnitCode();
      String measureUnitCode =
          StringUtils.isNotBlank(conditionMeasurementUnitCode)
              ? conditionMeasurementUnitCode
              : NO_UNIT_CODE;
      MeasureUnit measureUnit = MeasureUnit.valueOf(measureUnitCode);
      final String conditionMonetaryUnitCode = thresholdMeasureCondition.getConditionMonetaryUnitCode();
      String monetaryUnitCode = StringUtils.isNotBlank(conditionMonetaryUnitCode)? conditionMonetaryUnitCode: "GB";
      return StringUtils.replace(
          format(
              PRICE_PER_UNIT_BASED_DESCRIPTION,
              conditionCode.getThresholdType() == ThresholdType.MIN ? "more" : "less",
            MonetaryUnitCode.valueOf(monetaryUnitCode).getSymbol(),
              quantity,
              measureUnit.getUnit(locale, 1)),
          " ,",
          ",");
    } else if (threshold instanceof PriceBasedThresholdMeasureCondition) {
      PriceBasedThresholdMeasureCondition thresholdMeasureCondition =
          (PriceBasedThresholdMeasureCondition) threshold;
      int quantity = Double.valueOf(thresholdMeasureCondition.getConditionDutyAmount()).intValue();
      final String measureUnitCode = thresholdMeasureCondition.getConditionMonetaryUnitCode();
      String monetaryUnitCode = StringUtils.isNotBlank(measureUnitCode)? measureUnitCode: "GB";
      return StringUtils.replace(
          format(
              PRICE_BASED_DESCRIPTION,
              conditionCode.getThresholdType() == ThresholdType.MIN ? "more" : "less",
              MonetaryUnitCode.valueOf(monetaryUnitCode).getSymbol(),
              quantity),
          " ,",
          ",");
    } else {
      WeightOrVolumeOrUnitBasedThresholdMeasureCondition thresholdMeasureCondition =
          (WeightOrVolumeOrUnitBasedThresholdMeasureCondition) threshold;
      final String conditionMeasurementUnitCode =
          thresholdMeasureCondition.getConditionMeasurementUnitCode();
      String measureUnitCode =
          StringUtils.isNotBlank(conditionMeasurementUnitCode)
              ? conditionMeasurementUnitCode
              : NO_UNIT_CODE;
      MeasureUnit measureUnit = MeasureUnit.valueOf(measureUnitCode);
      int quantity = Double.valueOf(thresholdMeasureCondition.getConditionDutyAmount()).intValue();
      return StringUtils.replace(
          format(
              WEIGHT_VOLUME_DESCRIPTION_FORMAT,
              measureUnit.getVerb(locale),
              conditionCode.getThresholdType() == ThresholdType.MIN ? "more" : "less",
              quantity,
              measureUnit.getUnit(locale, quantity)),
          " ,",
          ",");
    }
  }
}
