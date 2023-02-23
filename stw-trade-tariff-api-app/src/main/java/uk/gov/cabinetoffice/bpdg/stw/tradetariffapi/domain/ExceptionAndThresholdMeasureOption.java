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

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption.THRESHOLD_MEASURE_OPTION_START_DESCRIPTION;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionAndThresholdMeasureOption extends DocumentCodeMeasureOption
  implements MeasureOption {

  private final ThresholdMeasureOption threshold;

  @Builder(builderMethodName = "exceptionAndThresholdBuilder")
  public ExceptionAndThresholdMeasureOption(
    @NonNull DocumentCodeDescription exception, ThresholdMeasureOption thresholdMeasure) {
    super(exception, 0);
    this.threshold = thresholdMeasure;
    this.type = MeasureOptionType.THRESHOLD_CERTIFICATE;
  }

  @Override
  public String getDescriptionOverlay() {
    return StringUtils.replaceOnceIgnoreCase(
      StringUtils.removeEndIgnoreCase(descriptionOverlay, "."), "Your", "If your")
      + " and "
      + StringUtils.removeStartIgnoreCase(
      threshold.getDescriptionOverlay(), THRESHOLD_MEASURE_OPTION_START_DESCRIPTION);
  }
}
