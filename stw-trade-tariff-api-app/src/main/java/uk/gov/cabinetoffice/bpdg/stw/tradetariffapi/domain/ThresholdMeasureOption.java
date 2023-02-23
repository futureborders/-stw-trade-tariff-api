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

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThresholdMeasureOption implements MeasureOption {

  public static final String THRESHOLD_MEASURE_OPTION_START_DESCRIPTION = "If your shipment ";

  private static final String DESCRIPTION_FORMAT =
    THRESHOLD_MEASURE_OPTION_START_DESCRIPTION + "%s %s than %s %s, then your goods are exempt.";
  private static final Pattern REQUIREMENT_REGEX =
    Pattern.compile("<span>(\\d*\\.?\\d*)</span> <abbr title='(.*)'>.*</abbr>");

  private final MeasureOptionType type;
  private final String requirement;
  private final MeasureConditionCode conditionCode;

  @Builder
  public ThresholdMeasureOption(@NonNull MeasureCondition threshold) {
    this.type = MeasureOptionType.THRESHOLD;
    this.requirement = threshold.getRequirement();

    var requirementMatcher = REQUIREMENT_REGEX.matcher(requirement);
    if (!requirementMatcher.find()) {
      throw new IllegalArgumentException(
          "Measure condition with requirement " + requirement + " does not match expected format " + REQUIREMENT_REGEX.pattern());
    }

    this.conditionCode = threshold.getConditionCode();
  }

  @Override
  public String getDescriptionOverlay() {
    var requirementMatcher = REQUIREMENT_REGEX.matcher(requirement);
    if (requirementMatcher.find()) {
      var measureUnit = MeasureUnit.getMeasureUnit(requirementMatcher.group(2));
      String quantity = requirementMatcher.group(1);
      return format(
        DESCRIPTION_FORMAT,
        measureUnit.getVerb(AppConfig.LOCALE),
        conditionCode.getThresholdType() == ThresholdType.MIN ? "more" : "less",
        Double.valueOf(quantity).intValue(),
        measureUnit.getUnit(AppConfig.LOCALE));
    }
    throw new IllegalArgumentException(
      "Measure condition with requirement " + requirement + " does not match expected format " + REQUIREMENT_REGEX.pattern());
  }
}
